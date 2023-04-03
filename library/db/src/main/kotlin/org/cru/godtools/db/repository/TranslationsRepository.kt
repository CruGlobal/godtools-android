package org.cru.godtools.db.repository

import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Translation

interface TranslationsRepository {
    suspend fun findTranslation(id: Long): Translation?
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

    suspend fun getTranslations() = getTranslationsFor()
    suspend fun getTranslationsFor(
        tools: Collection<String>? = null,
        languages: Collection<Locale>? = null,
    ): List<Translation>
    fun getTranslationsFlowFor(
        tools: Collection<String>? = null,
        languages: Collection<Locale>? = null,
    ): Flow<List<Translation>>

    // region DownloadManager Methods
    suspend fun markTranslationDownloaded(id: Long, isDownloaded: Boolean)
    // endregion DownloadManager Methods

    // region Initial Content Methods
    suspend fun storeInitialTranslations(translations: Collection<Translation>)
    // endregion Initial Content Methods
}
