package org.keynote.godtools.android.db.repository

import androidx.lifecycle.asLiveData
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.db.getAsFlow
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
class TranslationsRepository @Inject constructor(private val dao: GodToolsDao) {
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

    fun getLatestTranslationFlow(
        code: String?,
        locale: Locale?,
        isDownloaded: Boolean = false,
        trackAccess: Boolean = false
    ): Flow<Translation?> {
        if (code == null || locale == null) return flowOf(null)
        if (trackAccess) {
            val obj = Translation().apply { updateLastAccessed() }
            val where = TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(code, locale)
            @Suppress("DeferredResultUnused")
            dao.updateAsync(obj, where, TranslationTable.COLUMN_LAST_ACCESSED)
        }
        return getLatestTranslationQuery(code, locale, isDownloaded).getAsFlow(dao).map { it.firstOrNull() }
    }

    fun getLatestTranslationLiveData(
        code: String?,
        locale: Locale?,
        isDownloaded: Boolean = false,
        trackAccess: Boolean = false
    ) = getLatestTranslationFlow(code, locale, isDownloaded = isDownloaded, trackAccess = trackAccess).asLiveData()
    // endregion Latest Translations
}