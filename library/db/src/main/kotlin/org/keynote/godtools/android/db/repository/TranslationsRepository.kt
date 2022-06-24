package org.keynote.godtools.android.db.repository

import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.get
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
class TranslationsRepository @Inject constructor(private val dao: GodToolsDao) {
    // region Latest Translations
    suspend fun getLatestTranslation(
        code: String?,
        locale: Locale?,
        isDownloaded: Boolean = false
    ): Translation? = when {
        code == null || locale == null -> null
        else -> withContext(dao.coroutineDispatcher) {
            dao.getLatestTranslationQuery(code, locale, isPublished = true, isDownloaded = isDownloaded)
                .get(dao).firstOrNull()
        }
    }
    // endregion Latest Translations
}
