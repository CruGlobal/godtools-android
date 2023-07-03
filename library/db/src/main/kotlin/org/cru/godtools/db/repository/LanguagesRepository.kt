package org.cru.godtools.db.repository

import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Language

interface LanguagesRepository {
    suspend fun findLanguage(locale: Locale): Language?
    fun findLanguageFlow(locale: Locale): Flow<Language?>

    suspend fun getLanguages(): List<Language>
    fun getLanguagesFlow(): Flow<List<Language>>
    fun getLanguagesFlowForLocales(locales: Collection<Locale>): Flow<Collection<Language>>
    fun getLanguagesFlowForToolCategory(category: String): Flow<Collection<Language>>
    fun getPinnedLanguagesFlow(): Flow<List<Language>>

    suspend fun pinLanguage(locale: Locale)
    suspend fun unpinLanguage(locale: Locale)

    // region Initial Content Methods
    suspend fun storeInitialLanguages(languages: Collection<Language>)
    // endregion Initial Content Methods

    // region Sync Methods
    suspend fun storeLanguageFromSync(language: Language) = storeLanguagesFromSync(listOf(language))
    suspend fun storeLanguagesFromSync(languages: Collection<Language>)
    suspend fun removeLanguagesMissingFromSync(syncedLanguages: Collection<Language>)
    // endregion Sync Methods
}
