package org.cru.godtools.sync.repository

import javax.inject.Inject
import javax.inject.Singleton
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language

@Singleton
internal class SyncRepository @Inject constructor(
    private val languagesRepository: LanguagesRepository,
) {
    // region Languages
    fun storeLanguage(language: Language) {
        if (!language.isValid) return
        languagesRepository.storeLanguageFromSync(language)
    }
    // endregion Languages
}
