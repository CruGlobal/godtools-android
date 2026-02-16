package org.cru.godtools.ui.account.delete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import org.cru.godtools.account.GodToolsAccountManager

class DeleteAccountPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val accountManager: GodToolsAccountManager,
) : Presenter<DeleteAccountPresenter.UiState> {
    sealed interface UiState : CircuitUiState {
        data class Display(override val eventSink: (UiEvent) -> Unit = {}) : UiState
        data class Deleting(override val eventSink: (UiEvent) -> Unit = {}) : UiState
        data class Error(override val eventSink: (UiEvent) -> Unit = {}) : UiState

        val eventSink: (UiEvent) -> Unit
    }

    sealed interface UiEvent : CircuitUiEvent {
        data object DeleteAccount : UiEvent
        data object ClearError : UiEvent
        data object Close : UiEvent
    }

    @Composable
    override fun present(): UiState {
        val coroutineScope = rememberCoroutineScope()

        var deleting by remember { mutableStateOf(false) }
        var error by rememberRetained { mutableStateOf(false) }

        val eventSink: (UiEvent) -> Unit = remember {
            {
                when (it) {
                    UiEvent.DeleteAccount -> {
                        deleting = true
                        coroutineScope.launch {
                            if (accountManager.deleteAccount()) {
                                navigator.pop()
                            } else {
                                error = true
                                deleting = false
                            }
                        }
                    }

                    UiEvent.ClearError -> error = false

                    UiEvent.Close -> navigator.pop()
                }
            }
        }

        return when {
            error -> UiState.Error(eventSink)
            deleting -> UiState.Deleting(eventSink)
            else -> UiState.Display(eventSink)
        }
    }

    @AssistedFactory
    @CircuitInject(DeleteAccountScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): DeleteAccountPresenter
    }
}
