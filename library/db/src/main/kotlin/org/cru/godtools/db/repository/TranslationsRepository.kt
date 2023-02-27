package org.cru.godtools.db.repository

import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Translation

interface TranslationsRepository {
    suspend fun findLatestTranslation(
        code: String?,
        locale: Locale?,
        isDownloaded: Boolean = false
    ): Translation?
    fun findLatestTranslationFlow(
        code: String?,
        locale: Locale?,
        isDownloaded: Boolean = false,
        trackAccess: Boolean = false
    ): Flow<Translation?>
    fun getTranslationsFlowFor(tools: Collection<String>, languages: Collection<Locale>): Flow<List<Translation>>
}
