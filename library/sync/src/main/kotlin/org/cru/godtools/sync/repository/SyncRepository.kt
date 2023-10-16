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
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.User

@Singleton
internal class SyncRepository @Inject constructor(
    private val attachmentsRepository: AttachmentsRepository,
    private val languagesRepository: LanguagesRepository,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
    private val userRepository: UserRepository,
) {
    // region Tools
    suspend fun storeTools(tools: List<Tool>, existingTools: MutableSet<String>? = null, includes: Includes): Unit =
        coroutineScope {
            val validTools = tools.filter { it.isValid }
            if (validTools.isNotEmpty()) toolsRepository.storeToolsFromSync(validTools)

            val nestedTools = validTools
                .map { async { processIncludes(it, includes) } }
                .awaitAll()
                .flatMapTo(mutableSetOf()) { it }

            existingTools?.removeAll(validTools.mapTo(mutableSetOf()) { it.code })
            existingTools?.removeAll(nestedTools)

            // prune any existing tools that weren't synced and aren't already added to the device
            existingTools?.forEach { toolsRepository.deleteIfNotFavorite(it) }
        }

    /**
     * @return the tool codes that were stored in the database
     */
    private suspend fun processIncludes(tool: Tool, includes: Includes): Set<String> = coroutineScope {
        // persist related included objects
        if (includes.include(Tool.JSON_LATEST_TRANSLATIONS)) {
            tool.latestTranslations?.let { translations ->
                launch {
                    storeTranslations(
                        translations,
                        includes = includes.descendant(Tool.JSON_LATEST_TRANSLATIONS),
                        existing = tool.code?.let { translationsRepository.getTranslationsForTool(it) }
                            ?.map { it.id }
                            ?.toMutableSet()
                    )
                }
            }
        }
        if (includes.include(Tool.JSON_ATTACHMENTS)) {
            tool.attachments?.let { launch { attachmentsRepository.storeAttachmentsFromSync(tool, it) } }
        }

        val metatool = tool.metatool
            ?.takeIf { includes.include(Tool.JSON_METATOOL) }
            ?.let { async { storeTool(it, includes.descendant(Tool.JSON_METATOOL)) } }
            ?: CompletableDeferred(emptySet())
        val defaultVariant = tool.defaultVariant
            ?.takeIf { includes.include(Tool.JSON_DEFAULT_VARIANT) }
            ?.let { async { storeTool(it, includes.descendant(Tool.JSON_DEFAULT_VARIANT)) } }
            ?: CompletableDeferred(emptySet())

        metatool.await() + defaultVariant.await()
    }

    /**
     * @return the tool codes that were stored in the database
     */
    private suspend fun storeTool(tool: Tool, includes: Includes): Set<String> {
        if (!tool.isValid) return emptySet()

        toolsRepository.storeToolsFromSync(setOf(tool))
        return setOfNotNull(tool.code) + processIncludes(tool, includes)
    }

    suspend fun storeFavoriteTools(tools: List<Tool>, includes: Includes = Includes()) {
        storeTools(tools, includes = includes)
        toolsRepository.storeFavoriteToolsFromSync(tools)
    }
    // endregion Tools

    // region Languages
    @VisibleForTesting
    suspend fun storeLanguage(language: Language) {
        if (!language.isValid) return
        languagesRepository.storeLanguageFromSync(language)
    }
    // endregion Languages

    // region Translations
    private suspend fun storeTranslations(
        translations: List<Translation>,
        existing: MutableSet<Long>?,
        includes: Includes,
    ) {
        translations.forEach {
            if (storeTranslation(it, includes)) existing?.remove(it.id)
        }

        // prune any existing translations that weren't synced and aren't downloaded to the device
        existing?.forEach { translationsRepository.deleteTranslationIfNotDownloaded(it) }
    }

    private suspend fun storeTranslation(translation: Translation, includes: Includes): Boolean {
        if (!translation.isValid) return false
        if (includes.include(Translation.JSON_LANGUAGE)) translation.language?.let { storeLanguage(it) }
        translationsRepository.storeTranslationFromSync(translation)
        return true
    }
    // endregion Translations

    // region User
    suspend fun storeUser(user: User, includes: Includes = Includes()) {
        userRepository.storeUserFromSync(user)

        if (user.isInitialFavoriteToolsSynced && includes.include(User.JSON_FAVORITE_TOOLS)) {
            storeFavoriteTools(user.apiFavoriteTools, includes = includes.descendant(User.JSON_FAVORITE_TOOLS))
        }
    }
    // endregion User
}
