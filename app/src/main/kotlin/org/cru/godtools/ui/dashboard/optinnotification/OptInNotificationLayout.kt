package org.cru.godtools.ui.dashboard.optinnotification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import com.slack.circuit.overlay.OverlayEffect
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationPresenter.UiEvent
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationPresenter.UiState

@Composable
internal fun OptInNotificationLayout(state: UiState) {
    val eventSink by rememberUpdatedState(state.eventSink)

    if (state.showPrompt) {
        OverlayEffect {
            val result = show(OptInNotificationModalOverlay(isHardDenied = state.isHardDenied))
            eventSink(
                when (result) {
                    OptInNotificationModalOverlay.Result.AllowNotifications -> UiEvent.AllowNotifications
                    OptInNotificationModalOverlay.Result.Dismiss -> UiEvent.Dismiss
                }
            )
        }
    }
}
