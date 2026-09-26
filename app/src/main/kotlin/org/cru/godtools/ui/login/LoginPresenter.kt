package org.cru.godtools.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.account.LoginResponse
import org.cru.godtools.ui.login.LoginPresenter.UiState

class LoginPresenter @AssistedInject constructor(
    private val accountManager: GodToolsAccountManager,
    private val loginLauncherProducer: LoginLauncherProducer,
    @Assisted private val screen: LoginScreen,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    data class UiState(
        val createAccount: Boolean = false,
        val loginError: LoginResponse.Error? = null,
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data class Login(val type: AccountType) : UiEvent
        data object ClearError : UiEvent
        data object Close : UiEvent
    }

    @Composable
    override fun present(): UiState {
        CloseWhenAuthenticated()

        var loginError: LoginResponse.Error? by rememberSaveable { mutableStateOf(null) }
        val loginLauncher = loginLauncherProducer.produce(screen.createAccount) {
            when (it) {
                // CloseWhenAuthenticated() closes the screen once the account manager is authenticated
                LoginResponse.Success -> Unit
                is LoginResponse.Error -> loginError = it
            }
        }

        return UiState(
            createAccount = screen.createAccount,
            loginError = loginError,
        ) {
            when (it) {
                is UiEvent.Login -> loginLauncher(it.type)
                UiEvent.ClearError -> loginError = null
                UiEvent.Close -> navigator.pop()
            }
        }
    }

    @Composable
    private fun CloseWhenAuthenticated() {
        val isAuthenticated by remember { accountManager.isAuthenticatedFlow }.collectAsState(false)
        LaunchedEffect(isAuthenticated) { if (isAuthenticated) navigator.pop() }
    }

    @AssistedFactory
    @CircuitInject(LoginScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(screen: LoginScreen, navigator: Navigator): LoginPresenter
    }
}
