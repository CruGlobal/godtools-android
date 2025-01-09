package org.cru.godtools.ui.dashboard.home

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import org.cru.godtools.ui.banner.BannerType
import org.cru.godtools.ui.tools.ToolCard

@Parcelize
data object HomeScreen : Screen {
    data class UiState(
        val banner: BannerType? = null,
        val spotlightLessons: List<ToolCard.State> = emptyList(),
        val favoriteTools: List<ToolCard.State> = emptyList(),
        val favoriteToolsLoaded: Boolean = false,
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data object ViewAllFavorites : UiEvent
        data object ViewAllTools : UiEvent
    }
}
