package org.cru.godtools.ui.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.produceLatestTranslationState
import org.cru.godtools.db.repository.rememberAttachmentFile
import org.cru.godtools.db.repository.rememberLanguage
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation

@Singleton
class ToolCardPresenter @Inject constructor(
    private val fileSystem: ToolFileSystem,
    private val settings: Settings,
    private val attachmentsRepository: AttachmentsRepository,
    private val languagesRepository: LanguagesRepository,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
) {
    @Composable
    @OptIn(ExperimentalCoroutinesApi::class)
    fun present(
        tool: Tool,
        loadAppLanguage: Boolean = false,
        secondLanguage: Language? = null,
        loadAvailableLanguages: Boolean = false,
        eventSink: (ToolCard.Event) -> Unit = {},
    ): ToolCard.State {
        val coroutineScope = rememberCoroutineScope()
        val toolCode by rememberUpdatedState(tool.code)
        val appLocale by settings.produceAppLocaleState()
        val defaultLocale by rememberUpdatedState(tool.defaultLocale)

        // App Translation
        var appTranslation: Translation? by remember { mutableStateOf(null) }
        val appLanguage = if (loadAppLanguage) languagesRepository.rememberLanguage(appLocale) else null
        val appLanguageAvailable by remember { derivedStateOf { appTranslation != null } }

        // Translation
        var isLoaded by remember { mutableStateOf(false) }
        val translation by remember {
            val toolCodeFlow = snapshotFlow { toolCode }
            toolCodeFlow
                .combine(snapshotFlow { appLocale }) { t, l -> translationsRepository.findLatestTranslationFlow(t, l) }
                .flatMapLatest { it }
                .onEach { appTranslation = it }
                .combine(
                    toolCodeFlow
                        .combine(snapshotFlow { defaultLocale }) { t, l ->
                            translationsRepository.findLatestTranslationFlow(t, l)
                        }
                        .flatMapLatest { it }
                        .onStart { emit(null) }
                ) { t1, t2 -> t1 ?: t2 }
                .onEach { isLoaded = true }
        }.collectAsState(null)

        // Second Translation
        val secondTranslation by translationsRepository.produceLatestTranslationState(toolCode, secondLanguage?.code)
        val secondLanguageAvailable by remember { derivedStateOf { secondTranslation != null } }

        // eventSink
        val interceptingEventSink: (ToolCard.Event) -> Unit = remember(eventSink) {
            {
                when (it) {
                    ToolCard.Event.PinTool ->
                        coroutineScope.launch(NonCancellable) { toolCode?.let { toolsRepository.pinTool(it) } }
                    ToolCard.Event.UnpinTool ->
                        coroutineScope.launch(NonCancellable) { toolCode?.let { toolsRepository.unpinTool(it) } }
                    else -> eventSink(it)
                }
            }
        }

        return ToolCard.State(
            toolCode = toolCode,
            tool = tool,
            isLoaded = isLoaded,
            banner = attachmentsRepository.rememberAttachmentFile(fileSystem, tool.bannerId),
            // TODO: add support for specifying a locale for the ToolCardPresenter instead of just defaulting to the
            //       appLocale
            language = appLanguage,
            languageAvailable = appLanguageAvailable,
            translation = translation,
            appLanguage = appLanguage,
            appLanguageAvailable = appLanguageAvailable,
            secondLanguage = secondLanguage,
            secondLanguageAvailable = secondLanguageAvailable,
            availableLanguages = when {
                !loadAvailableLanguages -> 0
                else -> toolCode?.let { rememberAvailableLanguages(it) } ?: 0
            },
            eventSink = interceptingEventSink,
        )
    }

    @Composable
    private fun rememberAvailableLanguages(tool: String) = remember(tool) {
        translationsRepository.getTranslationsFlowForTool(tool)
            .map { it.distinctBy { it.languageCode }.count() }
            .distinctUntilChanged()
    }.collectAsState(0).value
}
