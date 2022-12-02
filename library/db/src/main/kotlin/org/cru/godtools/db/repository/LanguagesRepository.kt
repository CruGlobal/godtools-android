package org.cru.godtools.db.repository

import java.util.Locale
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Language

interface LanguagesRepository {
    fun getLanguageFlow(locale: Locale): Flow<Language?>
    fun getLanguagesForLocalesFlow(locales: Collection<Locale>): Flow<Collection<Language>>

    // region Sync Methods
    fun storeLanguageFromSync(language: Language) = storeLanguagesFromSync(listOf(language))
    fun storeLanguagesFromSync(languages: Collection<Language>)
    suspend fun removeLanguagesMissingFromSync(syncedLanguages: Collection<Language>)
    // endregion Sync Methods
}
