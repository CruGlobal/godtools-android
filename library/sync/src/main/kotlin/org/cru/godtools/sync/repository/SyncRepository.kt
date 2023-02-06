package org.cru.godtools.sync.repository

import androidx.collection.LongSparseArray
import androidx.collection.valueIterator
import javax.inject.Inject
import javax.inject.Singleton
import org.ccci.gto.android.common.jsonapi.util.Includes
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class SyncRepository @Inject constructor(
    private val dao: GodToolsDao,
    private val languagesRepository: LanguagesRepository,
) {
    // region Languages
    fun storeLanguage(language: Language) {
        if (!language.isValid) return
        languagesRepository.storeLanguageFromSync(language)
    }
    // endregion Languages

    // region Translations
    fun storeTranslations(
        translations: List<Translation>,
        existing: LongSparseArray<Translation>?,
        includes: Includes
    ) {
        translations.forEach {
            storeTranslation(it, includes)
            existing?.remove(it.id)
        }

        // prune any existing translations that weren't synced and aren't downloaded to the device
        existing?.valueIterator()?.forEach { translation ->
            dao.refresh(translation)?.takeUnless { it.isDownloaded }?.let { dao.delete(it) }
        }
    }

    private fun storeTranslation(translation: Translation, includes: Includes) {
        dao.updateOrInsert(
            translation,
            TranslationTable.COLUMN_TOOL, TranslationTable.COLUMN_LANGUAGE, TranslationTable.COLUMN_VERSION,
            TranslationTable.COLUMN_NAME, TranslationTable.COLUMN_DESCRIPTION, TranslationTable.COLUMN_TAGLINE,
            TranslationTable.COLUMN_DETAILS_OUTLINE, TranslationTable.COLUMN_DETAILS_BIBLE_REFERENCES,
            TranslationTable.COLUMN_DETAILS_CONVERSATION_STARTERS, TranslationTable.COLUMN_MANIFEST,
            TranslationTable.COLUMN_PUBLISHED
        )

        if (includes.include(Translation.JSON_LANGUAGE)) translation.language?.let { storeLanguage(it) }
    }
    // endregion Translations
}
