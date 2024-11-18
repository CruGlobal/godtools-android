package org.cru.godtools.ui.dashboard.home

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import org.cru.godtools.ui.tools.ToolCard

@Parcelize
object AllFavoritesScreen : Screen {
    data class UiState(val tools: List<ToolCard.State> = emptyList(), val eventSink: (UiEvent) -> Unit = {}) :
        CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data class MoveTool(val from: Int, val to: Int) : UiEvent
        data object CommitToolOrder : UiEvent
    }
}
