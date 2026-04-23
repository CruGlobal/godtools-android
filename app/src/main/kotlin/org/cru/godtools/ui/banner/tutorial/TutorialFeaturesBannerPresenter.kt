package org.cru.godtools.ui.banner.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.slack.circuit.runtime.CircuitUiEvent
import javax.inject.Inject
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.circuit.startCircuitActivity
import org.cru.godtools.shared.analytics.TutorialAnalyticsActionNames
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.cru.godtools.tutorial.layout.TutorialScreen
import org.cru.godtools.ui.banner.Banner
import org.cru.godtools.ui.banner.BannerPresenter
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerPresenter.UiState
import org.greenrobot.eventbus.EventBus

class TutorialFeaturesBannerPresenter @Inject internal constructor(
    private val eventBus: EventBus,
    private val settings: Settings,
) : BannerPresenter<UiState> {
    data class UiState(val eventSink: (UiEvent) -> Unit = {}) : Banner.UiState {
        override val type = Banner.Type.TUTORIAL_FEATURES
    }

    sealed interface UiEvent : CircuitUiEvent {
        data object OpenTutorial : UiEvent
        data object Dismiss : UiEvent
    }

    @Composable
    override fun present(): UiState? {
        val isDiscovered by remember { settings.isFeatureDiscoveredFlow(Settings.FEATURE_TUTORIAL_FEATURES) }
            .collectAsState(false)
        if (isDiscovered) return null

        val context = LocalContext.current
        return UiState { event ->
            when (event) {
                UiEvent.OpenTutorial -> {
                    // TODO: switch to using the navigator once the dashboard is fully Circuit
                    context.startCircuitActivity(TutorialScreen(PageSet.FEATURES))
                }

                UiEvent.Dismiss -> {
                    eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.BANNER_DISMISS))
                    settings.setFeatureDiscovered(Settings.FEATURE_TUTORIAL_FEATURES)
                }
            }
        }
    }
}
