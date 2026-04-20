package org.cru.godtools.ui.drawer

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.ui.drawer.DrawerMenuScreen.Event
import org.cru.godtools.ui.drawer.DrawerMenuScreen.State

@HiltViewModel
class DrawerViewModel @Inject constructor(private val accountManager: GodToolsAccountManager) : ViewModel() {
    val isAuthenticatedFlow = accountManager.isAuthenticatedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    // region Actions
    fun logout() = viewModelScope.launch { withContext(NonCancellable) { accountManager.logout() } }
    // endregion Actions

    @Composable
    fun toState(drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)): State {
        val scope = rememberCoroutineScope()

        return State(
            drawerState = drawerState,
            isLoggedIn = isAuthenticatedFlow.collectAsState().value,
            eventSink = {
                when (it) {
                    Event.Logout -> {
                        logout()
                        scope.launch { drawerState.close() }
                    }

                    Event.DismissDrawer -> scope.launch { drawerState.close() }
                }
            }
        )
    }
}
