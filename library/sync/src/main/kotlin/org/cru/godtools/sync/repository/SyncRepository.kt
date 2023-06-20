package org.cru.godtools.sync.repository

import androidx.annotation.VisibleForTesting
import javax.inject.Inject
import javax.inject.Singleton
import org.ccci.gto.android.common.jsonapi.util.Includes
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation

@Singleton
internal class SyncRepository @Inject constructor(
    private val attachmentsRepository: AttachmentsRepository,
    private val languagesRepository: LanguagesRepository,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
) {
    // region Tools
    suspend fun storeTools(tools: List<Tool>, existingTools: MutableSet<String>?, includes: Includes) {
        tools.forEach {
            storeTool(it, existingTools, includes)
            existingTools?.remove(it.code)
        }

        // prune any existing tools that weren't synced and aren't already added to the device
        existingTools?.forEach { toolsRepository.deleteIfNotFavorite(it) }
    }

    private fun storeTool(tool: Tool, existingTools: MutableSet<String>?, includes: Includes) {
        // don't store the tool if it's not valid
        if (!tool.isValid) return

        toolsRepository.storeToolFromSync(tool)

        // persist related included objects
        if (includes.include(Tool.JSON_LATEST_TRANSLATIONS)) {
            tool.latestTranslations?.let { translations ->
                storeTranslations(
                    translations,
                    includes = includes.descendant(Tool.JSON_LATEST_TRANSLATIONS),
                    existing = tool.code?.let { translationsRepository.getTranslationsForToolBlocking(it) }
                        ?.map { it.id }
                        ?.toMutableSet()
                )
            }
        }
        if (includes.include(Tool.JSON_ATTACHMENTS)) {
            tool.attachments?.let { attachments ->
                attachmentsRepository.storeAttachmentsFromSync(attachments)
                attachmentsRepository.removeAttachmentsMissingFromSync(tool, attachments)
            }
        }
        if (includes.include(Tool.JSON_METATOOL)) {
            tool.metatool?.let {
                storeTool(it, existingTools, includes.descendant(Tool.JSON_METATOOL))
                existingTools?.remove(it.code)
            }
        }
        if (includes.include(Tool.JSON_DEFAULT_VARIANT)) {
            tool.defaultVariant?.let {
                storeTool(it, existingTools, includes.descendant(Tool.JSON_DEFAULT_VARIANT))
                existingTools?.remove(it.code)
            }
        }
    }
    // endregion Tools

    // region Languages
    @VisibleForTesting
    fun storeLanguage(language: Language) {
        if (!language.isValid) return
        languagesRepository.storeLanguageFromSync(language)
    }
    // endregion Languages

    // region Translations
    private fun storeTranslations(
        translations: List<Translation>,
        existing: MutableSet<Long>?,
        includes: Includes
    ) {
        translations.forEach {
            if (storeTranslation(it, includes)) existing?.remove(it.id)
        }

        // prune any existing translations that weren't synced and aren't downloaded to the device
        existing?.forEach { translationsRepository.deleteTranslationIfNotDownloadedBlocking(it) }
    }

    private fun storeTranslation(translation: Translation, includes: Includes): Boolean {
        if (!translation.isValid) return false
        if (includes.include(Translation.JSON_LANGUAGE)) translation.language?.let { storeLanguage(it) }
        translationsRepository.storeTranslationFromSync(translation)
        return true
    }
    // endregion Translations
}
