package org.cru.godtools.ui.tooldetails

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.base.produceAppLocaleState
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.service.produceManifestState
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.produceLatestTranslationState
import org.cru.godtools.db.repository.produceToolState
import org.cru.godtools.db.repository.rememberAttachmentFile
import org.cru.godtools.db.repository.rememberLanguage
import org.cru.godtools.db.repository.rememberLatestTranslation
import org.cru.godtools.downloadmanager.GodToolsDownloadManager
import org.cru.godtools.downloadmanager.rememberDownloadProgress
import org.cru.godtools.model.Language
import org.cru.godtools.model.Language.Companion.getSortedDisplayNames
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen.Event
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen.State
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.cru.godtools.util.createToolIntent

class ToolDetailsPresenter @AssistedInject constructor(
    @ApplicationContext
    private val context: Context,
    private val attachmentsRepository: AttachmentsRepository,
    private val languagesRepository: LanguagesRepository,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
    private val downloadManager: GodToolsDownloadManager,
    private val fileSystem: ToolFileSystem,
    private val manifestManager: ManifestManager,
    private val settings: Settings,
    private val shortcutManager: GodToolsShortcutManager,
    private val toolCardPresenter: ToolCardPresenter,
    @Assisted private val screen: ToolDetailsScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<State> {
    @Composable
    override fun present(): State {
        val coroutineScope = rememberCoroutineScope()

        var toolCode by rememberSaveable { mutableStateOf(screen.initialTool) }

        val tool by toolsRepository.produceToolState(toolCode)
        val translation by rememberUpdatedState(rememberPrimaryTranslation(tool, toolCode))
        val secondTranslation by translationsRepository
            .produceLatestTranslationState(toolCode, screen.secondLanguage)
        val pendingShortcut by remember { derivedStateOf { shortcutManager.getPendingToolShortcut(toolCode) } }

        val eventSink: (Event) -> Unit = remember {
            {
                when (it) {
                    Event.NavigateUp -> navigator.pop()
                    Event.OpenTool -> {
                        val intent = tool?.createToolIntent(
                            context = context,
                            languages = listOfNotNull(translation?.languageCode, secondTranslation?.languageCode)
                        )

                        // TODO: record analytics event when launching a tool
                        if (intent != null) navigator.goTo(IntentScreen(intent))
                    }
                    Event.OpenToolTraining -> {
                        // TODO: launch tips tutorial when necessary

                        val intent = tool?.createToolIntent(
                            context = context,
                            languages = listOfNotNull(translation?.languageCode),
                            showTips = true
                        )

                        // TODO: record analytics event when launching a tool
                        if (intent != null) navigator.goTo(IntentScreen(intent))
                    }
                    is Event.SwitchVariant -> toolCode = it.variant
                    Event.PinTool -> {
                        coroutineScope.launch { toolsRepository.pinTool(toolCode) }
                        // TODO: trigger favorite tools sync
                        // TODO: mark favorite tools feature discovered
                    }
                    Event.UnpinTool -> {
                        coroutineScope.launch { toolsRepository.unpinTool(toolCode) }
                        // TODO: trigger favorite tools sync
                        // TODO: mark favorite tools feature discovered
                    }
                    Event.PinShortcut -> pendingShortcut?.let { shortcutManager.pinShortcut(it) }
                }
            }
        }

        val secondLanguage = languagesRepository.rememberLanguage(screen.secondLanguage)
        val variants = rememberVariants(tool?.metatoolCode, secondLanguage = secondLanguage, eventSink = eventSink)

        return State(
            toolCode = toolCode,
            tool = tool,
            banner = attachmentsRepository.rememberAttachmentFile(
                fileSystem = fileSystem,
                attachmentId = tool?.let { it.detailsBannerId ?: it.bannerId },
            ),
            bannerAnimation = attachmentsRepository.rememberAttachmentFile(fileSystem, tool?.detailsBannerAnimationId),
            downloadProgress = downloadManager.rememberDownloadProgress(toolCode, translation?.languageCode),
            hasShortcut = shortcutManager.canPinToolShortcut(tool),
            translation = translation,
            secondTranslation = secondTranslation,
            secondLanguage = secondLanguage,
            manifest = manifestManager.produceManifestState(translation).value,
            pages = rememberPages(hasVariants = variants.isNotEmpty()),
            availableLanguages = rememberAvailableLanguages(toolCode),
            variants = variants,
            eventSink = eventSink
        )
    }

    @Composable
    private fun rememberPrimaryTranslation(tool: Tool?, toolCode: String): Translation? {
        val appLocale by settings.produceAppLocaleState()

        return translationsRepository.rememberLatestTranslation(toolCode, appLocale)
            ?: translationsRepository.rememberLatestTranslation(toolCode, tool?.defaultLocale)
    }

    @Composable
    private fun rememberVariants(
        metaToolCode: String?,
        secondLanguage: Language?,
        eventSink: (Event) -> Unit,
    ): ImmutableList<ToolCard.State> {
        val eventSink by rememberUpdatedState(eventSink)

        return remember { toolsRepository.getNormalToolsFlow() }.collectAsState(emptyList()).value
            .filter { metaToolCode != null && it.metatoolCode == metaToolCode }
            .map { tool ->
                key(tool.code) {
                    toolCardPresenter.present(
                        tool = tool,
                        secondLanguage = secondLanguage,
                        loadAppLanguage = true,
                        loadAvailableLanguages = true,
                        eventSink = {
                            when (it) {
                                ToolCard.Event.Click -> tool.code?.let { eventSink(Event.SwitchVariant(it)) }
                                else -> Unit
                            }
                        }
                    )
                }
            }
            .toImmutableList()
    }

    @Composable
    private fun rememberPages(hasVariants: Boolean) = remember(hasVariants) {
        buildList {
            add(ToolDetailsPage.DESCRIPTION)
            if (hasVariants) add(ToolDetailsPage.VARIANTS)
        }.toImmutableList()
    }

    @Composable
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun rememberAvailableLanguages(code: String): ImmutableList<String> {
        val codeFlow = remember { MutableStateFlow(code) }.apply { value = code }

        return remember {
            codeFlow
                .flatMapLatest { translationsRepository.getTranslationsFlowForTool(it) }
                .map { it.mapTo(mutableSetOf()) { it.languageCode } }
                .distinctUntilChanged()
                .flatMapLatest { languagesRepository.getLanguagesFlowForLocales(it) }
                .combine(settings.appLanguageFlow) { langs, appLocale ->
                    langs.getSortedDisplayNames(context, appLocale).toImmutableList()
                }
        }.collectAsState(persistentListOf()).value
    }

    @AssistedFactory
    @CircuitInject(ToolDetailsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(screen: ToolDetailsScreen, navigator: Navigator): ToolDetailsPresenter
    }
}
