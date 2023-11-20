package org.cru.godtools.ui.account.delete

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object DeleteAccountScreen : Screen {
    sealed class State : CircuitUiState {
        data class Display(override val eventSink: (Event) -> Unit) : State()
        data class Deleting(override val eventSink: (Event) -> Unit) : State()
        data class Error(override val eventSink: (Event) -> Unit) : State()

        abstract val eventSink: (Event) -> Unit
    }

    sealed interface Event : CircuitUiEvent {
        data object DeleteAccount : Event
        data object ClearError : Event
        data object Close : Event
    }
}
