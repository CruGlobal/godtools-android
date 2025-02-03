package org.cru.godtools.ui.dashboard.tools

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.banner.BannerType
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.tools.ToolCard

@Parcelize
data object ToolsScreen : Screen {
    data class State(
        val banner: BannerType? = null,
        val spotlightTools: List<ToolCard.State> = emptyList(),
        val filters: Filters = Filters(),
        val tools: List<Tool> = emptyList(),
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    data class Filters(
        val categoryFilter: FilterMenu.UiState<String> = FilterMenu.UiState(),
        val languageFilter: FilterMenu.UiState<Language?> = FilterMenu.UiState(),
    ) : CircuitUiState {
        data class Filter<T>(val item: T, val count: Int)
    }

    sealed interface Event : CircuitUiEvent {
        data class OpenToolDetails(val tool: String, val source: String? = null) : Event
    }
}
