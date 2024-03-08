package org.cru.godtools.db.repository

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Translation

interface TranslationsRepository {
    suspend fun findTranslation(id: Long): Translation?
    suspend fun findLatestTranslation(code: String?, locale: Locale?, downloadedOnly: Boolean = false): Translation?
    fun findLatestTranslationFlow(code: String?, locale: Locale?, downloadedOnly: Boolean = false): Flow<Translation?>

    suspend fun getTranslations(): List<Translation>
    suspend fun getTranslationsForTool(tool: String): List<Translation>
    suspend fun getTranslationsForLanguages(languages: Collection<Locale>): List<Translation>

    fun getTranslationsFlow(): Flow<List<Translation>>
    fun getTranslationsFlowForTool(tool: String) = getTranslationsFlowForTools(listOf(tool))
    fun getTranslationsFlowForTools(tools: Collection<String>): Flow<List<Translation>>
    fun getTranslationsFlowForToolsAndLocales(
        tools: Collection<String>,
        locales: Collection<Locale>,
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
    suspend fun storeTranslationFromSync(translation: Translation)
    suspend fun deleteTranslationIfNotDownloaded(id: Long)
    // endregion Sync Methods
}

@Composable
fun TranslationsRepository.produceLatestTranslationState(
    code: String?,
    locale: Locale?,
    downloadedOnly: Boolean = false,
) = remember(code, locale, downloadedOnly) { findLatestTranslationFlow(code, locale, downloadedOnly) }
    .collectAsState(null)

@Composable
fun TranslationsRepository.rememberLatestTranslation(code: String?, locale: Locale?, downloadedOnly: Boolean = false) =
    produceLatestTranslationState(code, locale, downloadedOnly).value
