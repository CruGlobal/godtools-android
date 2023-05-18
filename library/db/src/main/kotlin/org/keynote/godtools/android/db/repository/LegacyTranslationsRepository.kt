package org.keynote.godtools.android.db.repository

import android.database.sqlite.SQLiteDatabase
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import org.ccci.gto.android.common.androidx.collection.WeakLruCache
import org.ccci.gto.android.common.androidx.collection.getOrPut
import org.ccci.gto.android.common.db.Expression.Companion.bind
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.db.findAsync
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class LegacyTranslationsRepository @Inject constructor(private val dao: GodToolsDao) : TranslationsRepository {
    override suspend fun findTranslation(id: Long) = dao.findAsync<Translation>(id).await()
    override suspend fun findLatestTranslation(code: String?, locale: Locale?, downloadedOnly: Boolean) = when {
        code == null || locale == null -> null
        else -> dao.getAsync(getLatestTranslationQuery(code, locale, downloadedOnly)).await().firstOrNull()
    }
    override fun findLatestTranslationFlow(
        code: String?,
        locale: Locale?,
        downloadedOnly: Boolean,
        trackAccess: Boolean,
    ) = getLatestTranslationFlow(code, locale, downloadedOnly, trackAccess)

    override suspend fun getTranslations() = dao.getAsync(Query.select<Translation>()).await()
    override suspend fun getTranslationsForLanguages(languages: Collection<Locale>) =
        dao.getAsync(getTranslationsForQuery(languages = languages)).await()
    override fun getTranslationsForToolBlocking(tool: String) = Query.select<Translation>()
        .where(TranslationTable.FIELD_TOOL.eq(bind(tool)))
        .get(dao)

    override fun getTranslationsFlow() = getTranslationsForQuery().getAsFlow(dao)
    override fun getTranslationsForToolsFlow(tools: Collection<String>) =
        getTranslationsForQuery(tools = tools).getAsFlow(dao)
    override fun getTranslationsForToolsAndLocalesFlow(
        tools: Collection<String>,
        locales: Collection<Locale>,
    ) = getTranslationsForQuery(tools = tools, languages = locales).getAsFlow(dao)

    private fun getTranslationsForQuery(
        tools: Collection<String>? = null,
        languages: Collection<Locale>? = null,
    ) = Query.select<Translation>()
        .run {
            when (tools) {
                null -> this
                else -> andWhere(TranslationTable.FIELD_TOOL.oneOf(tools.map { bind(it) }))
            }
        }
        .run {
            when (languages) {
                null -> this
                else -> andWhere(TranslationTable.FIELD_LANGUAGE.oneOf(languages.map { bind(it) }))
            }
        }

    // region Latest Translations
    private fun getLatestTranslationQuery(code: String, locale: Locale, downloadedOnly: Boolean) =
        Query.select<Translation>()
            .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(code, locale) and TranslationTable.SQL_WHERE_PUBLISHED)
            .run { if (downloadedOnly) andWhere(TranslationTable.SQL_WHERE_DOWNLOADED) else this }
            .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC)
            .limit(1)

    private val latestTranslationFlowCache =
        WeakLruCache<Triple<String, Locale, Boolean>, Flow<Translation?>>(maxSize = 20)

    private fun getLatestTranslationFlow(
        code: String?,
        locale: Locale?,
        isDownloaded: Boolean = false,
        trackAccess: Boolean = false,
    ): Flow<Translation?> {
        if (code == null || locale == null) return flowOf(null)
        if (trackAccess) {
            val obj = Translation().apply { updateLastAccessed() }
            val where = TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(code, locale)
            @Suppress("DeferredResultUnused")
            dao.updateAsync(obj, where, TranslationTable.COLUMN_LAST_ACCESSED)
        }
        return latestTranslationFlowCache.getOrPut(Triple(code, locale, isDownloaded)) { (code, locale, isDownloaded) ->
            getLatestTranslationQuery(code, locale, isDownloaded)
                .getAsFlow(dao).map { it.firstOrNull() }
                .shareIn(
                    dao.coroutineScope,
                    SharingStarted.WhileSubscribed(replayExpirationMillis = REPLAY_EXPIRATION),
                    replay = 1
                )
        }
    }
    // endregion Latest Translations

    override fun translationsChangeFlow() = dao.invalidationFlow(Translation::class.java, emitOnStart = true)

    // region DownloadManager Methods
    override suspend fun markTranslationDownloaded(id: Long, isDownloaded: Boolean) {
        val translation = Translation().also {
            it.id = id
            it.isDownloaded = isDownloaded
        }
        dao.updateAsync(translation, TranslationTable.COLUMN_DOWNLOADED).await()
    }

    override suspend fun markStaleTranslationsAsNotDownloaded() = dao.transactionAsync {
        val seen = mutableSetOf<TranslationKey>()
        val changes = Query.select<Translation>()
            .where(TranslationTable.SQL_WHERE_DOWNLOADED)
            .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC)
            .get(dao).asSequence()
            .filterNot { seen.add(TranslationKey(it)) }
            .sumOf {
                it.isDownloaded = false
                dao.update(it, TranslationTable.COLUMN_DOWNLOADED)
            }

        return@transactionAsync changes > 0
    }.await()
    // endregion DownloadManager Methods

    override suspend fun storeInitialTranslations(translations: Collection<Translation>) = dao.transactionAsync {
        translations.forEach { dao.insert(it, SQLiteDatabase.CONFLICT_IGNORE) }
    }.await()

    override suspend fun markBrokenManifestNotDownloaded(manifestName: String) {
        dao.updateAsync(
            Translation().apply { isDownloaded = false },
            TranslationTable.FIELD_MANIFEST.eq(manifestName),
            TranslationTable.COLUMN_DOWNLOADED
        ).await()
    }

    // region Sync Methods
    override fun storeTranslationFromSync(translation: Translation) = dao.updateOrInsert(
        translation,
        TranslationTable.COLUMN_TOOL, TranslationTable.COLUMN_LANGUAGE, TranslationTable.COLUMN_VERSION,
        TranslationTable.COLUMN_NAME, TranslationTable.COLUMN_DESCRIPTION, TranslationTable.COLUMN_TAGLINE,
        TranslationTable.COLUMN_DETAILS_OUTLINE, TranslationTable.COLUMN_DETAILS_BIBLE_REFERENCES,
        TranslationTable.COLUMN_DETAILS_CONVERSATION_STARTERS, TranslationTable.COLUMN_MANIFEST,
        TranslationTable.COLUMN_PUBLISHED
    )

    override fun deleteTranslationIfNotDownloadedBlocking(id: Long) {
        dao.transaction {
            dao.find<Translation>(id)
                ?.takeUnless { it.isDownloaded }
                ?.let { dao.delete(it) }
        }
    }
    // endregion Sync Methods
}
