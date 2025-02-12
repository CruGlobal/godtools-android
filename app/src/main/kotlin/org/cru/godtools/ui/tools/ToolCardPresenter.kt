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
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.db.repository.produceLatestTranslationState
import org.cru.godtools.db.repository.rememberAttachmentFile
import org.cru.godtools.db.repository.rememberLanguage
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.shared.user.activity.UserCounterNames.LESSON_COMPLETION

@Singleton
class ToolCardPresenter @Inject constructor(
    private val fileSystem: ToolFileSystem,
    private val settings: Settings,
    private val attachmentsRepository: AttachmentsRepository,
    private val languagesRepository: LanguagesRepository,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
    private val userCountersRepository: UserCountersRepository,
) {
    @Composable
    @OptIn(ExperimentalCoroutinesApi::class)
    fun present(
        tool: Tool,
        customLocale: Locale? = null,
        loadAppLanguage: Boolean = false,
        secondLanguage: Language? = null,
        loadAvailableLanguages: Boolean = false,
        eventSink: (ToolCard.Event) -> Unit = {},
    ): ToolCard.State {
        val coroutineScope = rememberCoroutineScope()
        val toolCode by rememberUpdatedState(tool.code)

        // App Translation
        val appLocale by settings.produceAppLocaleState()
        val appTranslationFlow = remember {
            combine(
                snapshotFlow { toolCode },
                snapshotFlow { appLocale },
            ) { t, l -> translationsRepository.findLatestTranslationFlow(t, l) }
                .flatMapLatest { it }
                .shareIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), 1)
        }
        val appTranslation by appTranslationFlow.collectAsState(null)
        val appLanguage = if (loadAppLanguage) languagesRepository.rememberLanguage(appLocale) else null

        // Custom Translation
        val customLocaleFlow = remember { MutableStateFlow(customLocale) }.apply { value = customLocale }
        val customLanguage = languagesRepository.rememberLanguage(customLocale)
        val customTranslationFlow = remember {
            combine(
                snapshotFlow { toolCode },
                customLocaleFlow
            ) { t, l -> translationsRepository.findLatestTranslationFlow(t, l) }
                .flatMapLatest { it }
                .shareIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), 1)
        }
        val customTranslation by customTranslationFlow.collectAsState(null)

        // Default Translation
        val defaultLocale by rememberUpdatedState(tool.defaultLocale)
        val defaultTranslationFlow = remember {
            combine(
                snapshotFlow { toolCode },
                snapshotFlow { defaultLocale }
            ) { t, l -> translationsRepository.findLatestTranslationFlow(t, l) }
                .flatMapLatest { it }
                .onStart { emit(null) }
        }

        // Primary Translation
        var isLoaded by remember { mutableStateOf(false) }
        val translation by remember {
            combine(
                customLocaleFlow.flatMapLatest { if (it != null) customTranslationFlow else appTranslationFlow },
                defaultTranslationFlow
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
            language = when (customLocale) {
                null -> appLanguage
                else -> customLanguage
            },
            languageAvailable = when (customLocale) {
                null -> appTranslation != null
                else -> customTranslation != null
            },
            translation = translation,
            appLanguage = appLanguage,
            appLanguageAvailable = appTranslation != null,
            secondLanguage = secondLanguage,
            secondLanguageAvailable = secondLanguageAvailable,
            progress = rememberProgress(toolCode, tool.progress),
            availableLanguages = when {
                !loadAvailableLanguages -> 0
                else -> toolCode?.let { rememberAvailableLanguages(it) } ?: 0
            },
            eventSink = interceptingEventSink,
        )
    }

    @Composable
    private fun rememberProgress(toolCode: String?, progress: Double?): ToolCard.State.Progress? {
        val completed by remember(toolCode) {
            when {
                toolCode == null -> flowOf(false)
                else -> userCountersRepository.findCounterFlow(LESSON_COMPLETION(toolCode))
                    .map { (it?.count ?: 0) > 0 }
            }
        }.collectAsState(false)

        return when {
            completed -> ToolCard.State.Progress.Completed
            progress != null -> ToolCard.State.Progress.InProgress(progress)
            else -> null
        }
    }

    @Composable
    private fun rememberAvailableLanguages(tool: String) = remember(tool) {
        translationsRepository.getTranslationsFlowForTool(tool)
            .map { it.distinctBy { it.languageCode }.count() }
            .distinctUntilChanged()
    }.collectAsState(0).value
}
