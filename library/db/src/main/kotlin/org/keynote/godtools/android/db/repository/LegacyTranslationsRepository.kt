package org.keynote.godtools.android.db.repository

import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.androidx.collection.WeakLruCache
import org.ccci.gto.android.common.androidx.collection.getOrPut
import org.ccci.gto.android.common.db.Expression
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class LegacyTranslationsRepository @Inject constructor(private val dao: GodToolsDao) : TranslationsRepository {
    override suspend fun findLatestTranslation(code: String?, locale: Locale?, isDownloaded: Boolean) =
        getLatestTranslation(code, locale, isDownloaded)
    override fun findLatestTranslationFlow(
        code: String?,
        locale: Locale?,
        isDownloaded: Boolean,
        trackAccess: Boolean,
    ) = getLatestTranslationFlow(code, locale, isDownloaded, trackAccess)

    override fun getTranslationsFlowFor(
        tools: Collection<String>,
        languages: Collection<Locale>
    ) = Query.select<Translation>()
        .where(TranslationTable.FIELD_TOOL.oneOf(tools.map { Expression.bind(it) }))
        .andWhere(TranslationTable.FIELD_LANGUAGE.oneOf(languages.map { Expression.bind(it) }))
        .getAsFlow(dao)

    // region Latest Translations
    private fun getLatestTranslationQuery(code: String, locale: Locale, isDownloaded: Boolean) =
        Query.select<Translation>()
            .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(code, locale) and TranslationTable.SQL_WHERE_PUBLISHED)
            .run { if (isDownloaded) andWhere(TranslationTable.SQL_WHERE_DOWNLOADED) else this }
            .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC)
            .limit(1)

    suspend fun getLatestTranslation(
        code: String?,
        locale: Locale?,
        isDownloaded: Boolean = false
    ): Translation? = when {
        code == null || locale == null -> null
        else -> withContext(dao.coroutineDispatcher) {
            getLatestTranslationQuery(code, locale, isDownloaded = isDownloaded).get(dao).firstOrNull()
        }
    }

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
}
