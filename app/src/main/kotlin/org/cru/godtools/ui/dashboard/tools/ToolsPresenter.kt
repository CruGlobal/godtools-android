package org.cru.godtools.ui.dashboard.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.map
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_ALL_TOOLS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_SPOTLIGHT
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.banner.Banner
import org.cru.godtools.ui.banner.BannerPresenter
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter
import org.cru.godtools.ui.dashboard.tools.ToolFiltersStateProducer.Filters
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.greenrobot.eventbus.EventBus

class ToolsPresenter @AssistedInject internal constructor(
    private val eventBus: EventBus,
    private val toolCardPresenter: ToolCardPresenter,
    private val toolsRepository: ToolsRepository,
    private val favoriteToolsBannerPresenter: BannerPresenter<FavoriteToolsBannerPresenter.UiState>,
    private val filteredToolsFlowProducer: FilteredToolsFlowProducer,
    private val toolFiltersStateProducer: ToolFiltersStateProducer,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    // region UiState / UiEvent
    data class UiState(
        val banner: Banner.UiState? = null,
        val dataLoaded: Boolean = true,
        val spotlightTools: List<ToolCard.State> = emptyList(),
        val filters: Filters = Filters(),
        val tools: List<ToolCard.State> = emptyList(),
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data class OpenToolDetails(val tool: String, val source: String? = null) : UiEvent
    }
    // endregion UiState / UiEvent

    @Composable
    override fun present(): UiState {
        val filters = toolFiltersStateProducer.produce()
        val selectedLocale by rememberUpdatedState(filters.languageFilter.selectedItem?.code)

        val eventSink: (UiEvent) -> Unit = remember {
            {
                when (it) {
                    is UiEvent.OpenToolDetails -> {
                        if (it.source != null) {
                            eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, it.tool, it.source))
                        }
                        navigator.goTo(ToolDetailsScreen(it.tool, selectedLocale))
                    }
                }
            }
        }

        val spotlightTools = rememberSpotlightTools(
            secondLanguage = filters.languageFilter.selectedItem,
            eventSink = eventSink
        )
        val tools = rememberTools(
            category = filters.categoryFilter.selectedItem,
            language = filters.languageFilter.selectedItem,
            eventSink = eventSink,
        )

        return UiState(
            banner = favoriteToolsBannerPresenter.present(),
            dataLoaded = spotlightTools != null && tools != null,
            spotlightTools = spotlightTools.orEmpty(),
            filters = filters,
            tools = tools.orEmpty(),
            eventSink = eventSink,
        )
    }

    @Composable
    private fun rememberSpotlightTools(
        secondLanguage: Language?,
        eventSink: (UiEvent) -> Unit,
    ): List<ToolCard.State>? {
        val tools by remember {
            toolsRepository.getNormalToolsFlow()
                .map { it.filter { !it.isHidden && it.isSpotlight }.sortedWith(Tool.COMPARATOR_DEFAULT_ORDER) }
        }.collectAsState(null)
        val eventSink by rememberUpdatedState(eventSink)

        return tools?.map { tool ->
            val toolCode by rememberUpdatedState(tool.code)

            toolCardPresenter.present(
                tool = tool,
                secondLanguage = secondLanguage,
                eventSink = {
                    when (it) {
                        ToolCard.Event.Click,
                        ToolCard.Event.OpenTool,
                        ToolCard.Event.OpenToolDetails ->
                            toolCode?.let { eventSink(UiEvent.OpenToolDetails(it, SOURCE_SPOTLIGHT)) }

                        ToolCard.Event.PinTool,
                        ToolCard.Event.UnpinTool -> error("$it should be handled by the ToolCardPresenter")
                    }
                }
            )
        }
    }

    @Composable
    private fun rememberTools(
        category: String?,
        language: Language?,
        eventSink: (UiEvent) -> Unit,
    ): List<ToolCard.State>? {
        val locale = language?.code
        val tools by remember(category, locale) { filteredToolsFlowProducer.getFlow(category, locale) }
            .collectAsState(null)
        val eventSink by rememberUpdatedState(eventSink)

        return tools?.map { tool ->
            key(tool.code) {
                val toolCode by rememberUpdatedState(tool.code)
                toolCardPresenter.present(
                    tool = tool,
                    secondLanguage = language,
                    eventSink = {
                        when (it) {
                            ToolCard.Event.Click,
                            ToolCard.Event.OpenTool,
                            ToolCard.Event.OpenToolDetails ->
                                toolCode?.let { eventSink(UiEvent.OpenToolDetails(it, SOURCE_ALL_TOOLS)) }

                            ToolCard.Event.PinTool,
                            ToolCard.Event.UnpinTool -> error("$it should be handled by the ToolCardPresenter")
                        }
                    }
                )
            }
        }
    }

    @AssistedFactory
    @CircuitInject(ToolsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): ToolsPresenter
    }
}
