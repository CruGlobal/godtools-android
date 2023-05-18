package org.cru.godtools.db.repository

import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Language

interface LanguagesRepository {
    suspend fun findLanguage(locale: Locale): Language?
    fun findLanguageFlow(locale: Locale): Flow<Language?>
    suspend fun getLanguages(): List<Language>
    fun getLanguagesFlowForLocales(locales: Collection<Locale>): Flow<Collection<Language>>

    // region Initial Content Methods
    suspend fun storeInitialLanguages(languages: Collection<Language>)
    // endregion Initial Content Methods

    // region Sync Methods
    fun storeLanguageFromSync(language: Language) = storeLanguagesFromSync(listOf(language))
    fun storeLanguagesFromSync(languages: Collection<Language>)
    suspend fun removeLanguagesMissingFromSync(syncedLanguages: Collection<Language>)
    // endregion Sync Methods
}
