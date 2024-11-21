package org.cru.godtools.ui.dashboard.home

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.banner.BannerType

@Parcelize
internal data object HomeScreen : Screen {
    data class UiState(
        val banner: BannerType? = null,
        val spotlightLessons: List<String> = emptyList(),
        val favoriteTools: List<Tool> = emptyList(),
        val favoriteToolsLoaded: Boolean = false,
    ) : CircuitUiState
}
