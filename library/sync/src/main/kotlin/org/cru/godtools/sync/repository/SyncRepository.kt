package org.cru.godtools.sync.repository

import androidx.annotation.VisibleForTesting
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
    suspend fun storeTools(tools: List<Tool>, existingTools: MutableSet<String>?, includes: Includes) = coroutineScope {
        val stored = tools
            .map { async { storeTool(it, includes) } }
            .awaitAll()
            .flatMapTo(mutableSetOf()) { it }
        existingTools?.removeAll(stored)

        // prune any existing tools that weren't synced and aren't already added to the device
        existingTools?.forEach { toolsRepository.deleteIfNotFavorite(it) }
    }

    /**
     * @return the tool codes that were stored in the database
     */
    private suspend fun storeTool(tool: Tool, includes: Includes): Set<String> = coroutineScope {
        // don't store the tool if it's not valid
        if (!tool.isValid) return@coroutineScope emptySet()
        toolsRepository.storeToolFromSync(tool)

        // persist related included objects
        if (includes.include(Tool.JSON_LATEST_TRANSLATIONS)) {
            tool.latestTranslations?.let { translations ->
                launch {
                    storeTranslations(
                        translations,
                        includes = includes.descendant(Tool.JSON_LATEST_TRANSLATIONS),
                        existing = tool.code?.let { translationsRepository.getTranslationsForToolBlocking(it) }
                            ?.map { it.id }
                            ?.toMutableSet()
                    )
                }
            }
        }
        if (includes.include(Tool.JSON_ATTACHMENTS)) {
            tool.attachments?.let { attachments ->
                launch {
                    attachmentsRepository.storeAttachmentsFromSync(attachments)
                    attachmentsRepository.removeAttachmentsMissingFromSync(tool, attachments)
                }
            }
        }

        val metatool = when {
            includes.include(Tool.JSON_METATOOL) -> async {
                tool.metatool?.let { storeTool(it, includes.descendant(Tool.JSON_METATOOL)) }
            }
            else -> CompletableDeferred(emptySet())
        }
        val defaultVariant = when {
            includes.include(Tool.JSON_DEFAULT_VARIANT) -> async {
                tool.defaultVariant?.let { storeTool(it, includes.descendant(Tool.JSON_DEFAULT_VARIANT)) }
            }
            else -> CompletableDeferred(emptySet())
        }

        setOfNotNull(tool.code) + metatool.await().orEmpty() + defaultVariant.await().orEmpty()
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
