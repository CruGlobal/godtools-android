package org.cru.godtools.ui.dashboard.optinnotification

import androidx.compose.runtime.Composable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator

interface OptInNotificationPresenter {
    data class UiState(
        val showPrompt: Boolean = false,
        val isHardDenied: Boolean = false,
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data object AllowNotifications : UiEvent
        data object Dismiss : UiEvent
    }

    @Composable
    fun present(navigator: Navigator): UiState
}
