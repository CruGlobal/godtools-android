package org.cru.godtools.ui.drawer

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.runtime.presenter.Presenter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.ui.drawer.DrawerMenuScreen.Event
import org.cru.godtools.ui.drawer.DrawerMenuScreen.State

@Singleton
class DrawerMenuPresenter @Inject constructor(
    private val accountManager: GodToolsAccountManager,
) : Presenter<State> {
    @Composable
    override fun present(): State {
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(DrawerValue.Closed)

        return State(
            drawerState = drawerState,
            isLoggedIn = remember { accountManager.isAuthenticatedFlow }.collectAsState(false).value,
            eventSink = {
                when (it) {
                    Event.Logout -> scope.launch {
                        launch(NonCancellable) { accountManager.logout() }
                        drawerState.close()
                    }

                    Event.DismissDrawer -> scope.launch { drawerState.close() }
                }
            }
        )
    }
}
