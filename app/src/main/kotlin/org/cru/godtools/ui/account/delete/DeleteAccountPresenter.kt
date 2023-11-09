package org.cru.godtools.ui.account.delete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.retained.rememberRetained
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
) : Presenter<DeleteAccountScreen.State> {
    @Composable
    override fun present(): DeleteAccountScreen.State {
        val coroutineScope = rememberCoroutineScope()
        var error by rememberRetained { mutableStateOf(false) }

        return DeleteAccountScreen.State {
            when (it) {
                DeleteAccountScreen.Event.DeleteAccount -> {
                    coroutineScope.launch {
                        if (accountManager.deleteAccount()) {
                            navigator.pop()
                        } else {
                            error = true
                        }
                    }
                }
                DeleteAccountScreen.Event.Cancel -> navigator.pop()
            }
        }
    }

    @AssistedFactory
    @CircuitInject(DeleteAccountScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): DeleteAccountPresenter
    }
}
