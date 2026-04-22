package org.cru.godtools.ui.banner.favoritetools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.CircuitUiEvent
import javax.inject.Inject
import org.cru.godtools.base.Settings
import org.cru.godtools.ui.banner.Banner
import org.cru.godtools.ui.banner.BannerPresenter
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter.UiState

class FavoriteToolsBannerPresenter @Inject constructor(private val settings: Settings) : BannerPresenter<UiState> {
    data class UiState(val eventSink: (UiEvent) -> Unit = {}) : Banner.UiState {
        override val type = Banner.Type.TOOL_LIST_FAVORITES
    }

    sealed interface UiEvent : CircuitUiEvent {
        data object Dismiss : UiEvent
    }

    @Composable
    override fun present(): UiState? {
        val isFeatureDiscovered by remember { settings.isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE) }
            .collectAsState(initial = false)
        if (isFeatureDiscovered) return null

        return UiState { event ->
            when (event) {
                is UiEvent.Dismiss -> settings.setFeatureDiscovered(Settings.FEATURE_TOOL_FAVORITE)
            }
        }
    }
}
