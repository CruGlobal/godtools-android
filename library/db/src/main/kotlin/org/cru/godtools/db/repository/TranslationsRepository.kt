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
    fun getTranslationsForToolBlocking(tool: String): List<Translation>
    fun getTranslationsForToolFlow(tool: String): Flow<List<Translation>> = getTranslationsFlowFor(tools = listOf(tool))
    suspend fun getTranslationsFor(
        tools: Collection<String>? = null,
        languages: Collection<Locale>? = null,
    ): List<Translation>
    fun getTranslationsFlow() = getTranslationsFlowFor()
    fun getTranslationsFlowFor(
        tools: Collection<String>? = null,
        languages: Collection<Locale>? = null,
    ): Flow<List<Translation>>

    fun translationsChangeFlow(): Flow<Any?>

    // region DownloadManager Methods
    suspend fun markTranslationDownloaded(id: Long, isDownloaded: Boolean)
    suspend fun markStaleTranslationsAsNotDownloaded(): Boolean
    // endregion DownloadManager Methods

    // region Initial Content Methods
    suspend fun storeInitialTranslations(translations: Collection<Translation>)
    // endregion Initial Content Methods

    // region ManifestManager Methods
    suspend fun markBrokenManifestNotDownloaded(manifestName: String)
    // endregion ManifestManager Methods

    // region Sync Methods
    fun storeTranslationFromSync(translation: Translation)
    fun deleteTranslationIfNotDownloadedBlocking(id: Long)
    // endregion Sync Methods
}
