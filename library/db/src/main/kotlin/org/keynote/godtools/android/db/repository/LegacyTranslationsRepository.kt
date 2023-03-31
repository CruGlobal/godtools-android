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
import org.ccci.gto.android.common.db.findAsync
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class LegacyTranslationsRepository @Inject constructor(private val dao: GodToolsDao) : TranslationsRepository {
    override suspend fun findTranslation(id: Long) = dao.findAsync<Translation>(id).await()
    override suspend fun findLatestTranslation(code: String?, locale: Locale?, isDownloaded: Boolean) = when {
        code == null || locale == null -> null
        else -> dao.getAsync(getLatestTranslationQuery(code, locale, isDownloaded = isDownloaded)).await().firstOrNull()
    }
    override fun findLatestTranslationFlow(
        code: String?,
        locale: Locale?,
        isDownloaded: Boolean,
        trackAccess: Boolean,
    ) = getLatestTranslationFlow(code, locale, isDownloaded, trackAccess)

    override suspend fun getTranslationsFor(tools: Collection<String>?, languages: Collection<Locale>?) =
        dao.getAsync(getTranslationsForQuery(tools = tools, languages = languages)).await()
    override fun getTranslationsFlowFor(tools: Collection<String>?, languages: Collection<Locale>?) =
        getTranslationsForQuery(tools = tools, languages = languages).getAsFlow(dao)
    private fun getTranslationsForQuery(
        tools: Collection<String>?,
        languages: Collection<Locale>?,
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
    private fun getLatestTranslationQuery(code: String, locale: Locale, isDownloaded: Boolean) =
        Query.select<Translation>()
            .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(code, locale) and TranslationTable.SQL_WHERE_PUBLISHED)
            .run { if (isDownloaded) andWhere(TranslationTable.SQL_WHERE_DOWNLOADED) else this }
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

    override suspend fun storeInitialTranslations(translations: Collection<Translation>) = dao.transactionAsync {
        translations.forEach { dao.insert(it, SQLiteDatabase.CONFLICT_IGNORE) }
    }.await()
}
