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
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_FAVORITE
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.dashboard.home.AllFavoritesScreen.UiEvent
import org.cru.godtools.ui.dashboard.home.AllFavoritesScreen.UiState
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.cru.godtools.util.createToolIntent
import org.greenrobot.eventbus.EventBus

class AllFavoritesPresenter @AssistedInject constructor(
    @ApplicationContext
    private val context: Context,
    private val eventBus: EventBus,
    private val toolCardPresenter: ToolCardPresenter,
    private val toolsRepository: ToolsRepository,
    @Assisted
    private val navigator: Navigator,
) : Presenter<UiState> {
    @Composable
    override fun present(): UiState {
        val scope = rememberCoroutineScope()
        var tools by rememberFavoriteTools()

        return UiState(
            tools = tools.mapNotNull { tool ->
                val toolCode = tool.code ?: return@mapNotNull null
                key(toolCode) {
                    lateinit var state: ToolCard.State
                    state = toolCardPresenter.present(tool) {
                        when (it) {
                            ToolCard.Event.Click,
                            ToolCard.Event.OpenTool -> {
                                val intent = tool.createToolIntent(
                                    context = context,
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
                            ToolCard.Event.OpenToolDetails -> {
                                eventBus.post(
                                    OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, toolCode, SOURCE_FAVORITE)
                                )
                                navigator.goTo(ToolDetailsScreen(toolCode))
                            }
                            ToolCard.Event.PinTool,
                            ToolCard.Event.UnpinTool -> error("$it should be handled by the ToolCardPresenter")
                        }
                    }
                    state
                }
            }
        ) {
            when (it) {
                is UiEvent.MoveTool -> tools = tools.toMutableList().apply { add(it.to, removeAt(it.from)) }
                UiEvent.CommitToolOrder -> scope.launch(NonCancellable) {
                    toolsRepository.storeToolOrder(tools.mapNotNull { it.code })
                }
            }
        }
    }

    @Composable
    private fun rememberFavoriteTools(): MutableState<List<Tool>> {
        val state = remember { mutableStateOf(emptyList<Tool>()) }
        LaunchedEffect(Unit) { toolsRepository.getFavoriteToolsFlow().collect { state.value = it } }
        return state
    }

    @AssistedFactory
    @CircuitInject(AllFavoritesScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): AllFavoritesPresenter
    }
}
