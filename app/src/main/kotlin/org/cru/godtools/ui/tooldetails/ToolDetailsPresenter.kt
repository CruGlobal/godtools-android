package org.cru.godtools.ui.tooldetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.toImmutableList
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.model.Language.Companion.getSortedDisplayNames
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolViewModels
import org.cru.godtools.util.createToolIntent

class ToolDetailsPresenter @AssistedInject constructor(
    private val shortcutManager: GodToolsShortcutManager,
    @Assisted private val screen: ToolDetailsScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<ToolDetailsScreen.State> {
    @Composable
    override fun present(): ToolDetailsScreen.State {
        val toolViewModels: ToolViewModels = viewModel(key = "ToolDetailsContent")
        val viewModel: ToolDetailsViewModel = viewModel()

        val toolCode by viewModel.toolCode.collectAsState()
        val toolViewModel = toolViewModels[toolCode.orEmpty()]
        val tool by toolViewModel.tool.collectAsState()
        val shortcut by viewModel.shortcut.collectAsState()
        val translation by toolViewModel.firstTranslation.collectAsState()
        val secondTranslation by toolViewModel.secondTranslation.collectAsState()

        val context = LocalContext.current
        val appLocale = LocalAppLanguage.current
        val rawLanguages by toolViewModel.availableLanguages.collectAsState()
        val languages by remember(context, appLocale) {
            derivedStateOf { rawLanguages.getSortedDisplayNames(context, appLocale).toImmutableList() }
        }

        val eventSink: (ToolDetailsScreen.Event) -> Unit = remember(viewModel) {
            {
                when (it) {
                    ToolDetailsScreen.Event.NavigateUp -> navigator.pop()
                    ToolDetailsScreen.Event.OpenTool -> {
                        val intent = tool?.createToolIntent(
                            context = context,
                            languages = listOfNotNull(translation.value?.languageCode, secondTranslation?.languageCode)
                        )

                        // TODO: record analytics event when launching a tool
                        if (intent != null) navigator.goTo(IntentScreen(intent))
                    }
                    ToolDetailsScreen.Event.OpenToolTraining -> {
                        // TODO: launch tips tutorial when necessary

                        val intent = tool?.createToolIntent(
                            context = context,
                            languages = listOfNotNull(translation.value?.languageCode),
                            showTips = true
                        )

                        // TODO: record analytics event when launching a tool
                        if (intent != null) navigator.goTo(IntentScreen(intent))
                    }
                    is ToolDetailsScreen.Event.SwitchVariant -> viewModel.setToolCode(it.variant)
                    ToolDetailsScreen.Event.PinTool -> toolViewModel.pinTool()
                    ToolDetailsScreen.Event.UnpinTool -> toolViewModel.unpinTool()
                    ToolDetailsScreen.Event.PinShortcut -> shortcut?.let { shortcutManager.pinShortcut(it) }
                }
            }
        }

        return ToolDetailsScreen.State(
            toolCode = toolCode,
            tool = tool,
            banner = toolViewModel.detailsBanner.collectAsState().value,
            bannerAnimation = toolViewModel.detailsBannerAnimation.collectAsState().value,
            downloadProgress = toolViewModel.downloadProgress.collectAsState().value,
            hasShortcut = shortcut != null,
            translation = translation.value,
            secondTranslation = secondTranslation,
            secondLanguage = toolViewModel.secondLanguage.collectAsState().value,
            manifest = toolViewModel.firstManifest.collectAsState().value,
            pages = viewModel.pages.collectAsState().value.toImmutableList(),
            availableLanguages = languages,
            variants = viewModel.variants.collectAsState().value.mapNotNull {
                it.code?.let { code ->
                    toolViewModels[code, it].toState(
                        eventSink = { e ->
                            when (e) {
                                ToolCard.Event.Click -> eventSink(ToolDetailsScreen.Event.SwitchVariant(code))
                                else -> Unit
                            }
                        }
                    )
                }
            },
            eventSink = eventSink
        )
    }

    @AssistedFactory
    @CircuitInject(ToolDetailsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(screen: ToolDetailsScreen, navigator: Navigator): ToolDetailsPresenter
    }
}
