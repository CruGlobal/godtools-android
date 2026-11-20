package org.cru.godtools.ui.dashboard.home

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_FAVORITE
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.banner.Banner
import org.cru.godtools.ui.banner.BannerPresenter
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerPresenter
import org.cru.godtools.ui.dashboard.home.HomePresenter.UiState
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.cru.godtools.ui.tools.ToolCardPresenter.ToolCardEvent
import org.cru.godtools.util.createToolIntent
import org.greenrobot.eventbus.EventBus

class HomePresenter @AssistedInject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val eventBus: EventBus,
    private val toolCardPresenter: ToolCardPresenter,
    private val toolsRepository: ToolsRepository,
    private val tutorialFeaturesBannerPresenter: BannerPresenter<TutorialFeaturesBannerPresenter.UiState>,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    // region UiState / UiEvent
    data class UiState(
        val dataLoaded: Boolean = true,
        val banner: Banner.UiState? = null,
        val favoriteTools: List<ToolCardPresenter.UiState> = emptyList(),
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data class MoveTool(val from: Int, val to: Int) : UiEvent
        data object CommitToolOrder : UiEvent
        data object ViewAllTools : UiEvent
    }
    // endregion UiState / UiEvent

    @Composable
    override fun present(): UiState {
        val scope = rememberCoroutineScope()
        var favoriteTools by rememberFavoriteTools()

        return UiState(
            dataLoaded = favoriteTools != null,
            banner = tutorialFeaturesBannerPresenter.present(),
            favoriteTools = favoriteTools.orEmpty().toToolCardStates(),
        ) {
            when (it) {
                is UiEvent.MoveTool ->
                    favoriteTools = favoriteTools.orEmpty().toMutableList().apply { add(it.to, removeAt(it.from)) }

                UiEvent.CommitToolOrder -> scope.launch(start = CoroutineStart.UNDISPATCHED) {
                    withContext(NonCancellable) {
                        toolsRepository.storeToolOrder(favoriteTools.orEmpty().mapNotNull { tool -> tool.code })
                    }
                }

                UiEvent.ViewAllTools -> navigator.resetRoot(ToolsScreen, Navigator.StateOptions.SaveAndRestore)
            }
        }
    }

    @Composable
    private fun rememberFavoriteTools(): MutableState<List<Tool>?> {
        val state = remember { mutableStateOf<List<Tool>?>(null) }
        LaunchedEffect(Unit) { toolsRepository.getFavoriteToolsFlow().collect { state.value = it } }
        return state
    }

    @Composable
    private fun List<Tool>.toToolCardStates() = mapNotNull { tool ->
        val toolCode = tool.code ?: return@mapNotNull null

        key(toolCode) {
            lateinit var state: ToolCardPresenter.UiState
            state = toolCardPresenter.present(tool) {
                when (it) {
                    ToolCardEvent.Click,
                    ToolCardEvent.OpenTool -> {
                        val intent = context.createToolIntent(
                            tool = tool,
                            languages = listOfNotNull(
                                tool.primaryLocale ?: state.translation?.languageCode,
                                tool.parallelLocale
                            ),
                            saveLanguageSettings = true
                        )

                        if (intent != null) {
                            eventBus.post(
                                OpenAnalyticsActionEvent(ACTION_OPEN_TOOL, tool.code, SOURCE_FAVORITE)
                            )
                            navigator.goTo(IntentScreen(intent))
                        }
                    }

                    ToolCardEvent.OpenToolDetails -> {
                        eventBus.post(
                            OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, toolCode, SOURCE_FAVORITE)
                        )
                        navigator.goTo(ToolDetailsScreen(toolCode))
                    }
                }
            }
            state
        }
    }

    @AssistedFactory
    @CircuitInject(HomeScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }
}
