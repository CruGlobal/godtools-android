package org.cru.godtools.ui.dashboard

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Navigator
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.navigation.intercepting.rememberInterceptingNavigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.sync.SyncTracker
import org.ccci.gto.android.common.sync.rememberSyncTracker
import org.cru.godtools.base.ui.circuit.screen.dashboard.DashboardScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.dashboard.DashboardPresenter.UiState
import org.cru.godtools.ui.drawer.DrawerMenuPresenter
import org.cru.godtools.ui.drawer.DrawerMenuScreen

class DashboardPresenter @AssistedInject internal constructor(
    private val drawerMenuPresenter: DrawerMenuPresenter,
    private val syncService: GodToolsSyncService,
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: DashboardScreen,
) : Presenter<UiState> {
    @ConsistentCopyVisibility
    data class UiState internal constructor(
        val drawerState: DrawerMenuScreen.State = DrawerMenuScreen.State(),
        val isSyncing: Boolean = false,
        val pageBackStack: BackStack<SaveableBackStack.Record> = SaveableBackStack(HomeScreen),
        val pageNavigator: Navigator = Navigator(pageBackStack) {},
        val snackbarState: SnackbarHostState = SnackbarHostState(),
        internal val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    internal sealed interface UiEvent : CircuitUiEvent {
        data object TriggerSync : UiEvent
    }

    @Composable
    override fun present(): UiState {
        val syncTracker = rememberSyncTracker { it.syncData() }

        val pageBackStack = rememberSaveableBackStack(screen.initialPage)
        val pageNavigator = rememberCircuitNavigator(pageBackStack) { navigator.pop(it) }

        return UiState(
            drawerState = drawerMenuPresenter.present(),
            isSyncing = syncTracker.isSyncing.collectAsState().value,
            pageBackStack = pageBackStack,
            pageNavigator = rememberInterceptingNavigator(
                navigator,
                interceptors = remember(pageNavigator) { listOf(DashboardPageNavigationInterceptor(pageNavigator)) }
            ),
            snackbarState = remember { SnackbarHostState() },
        ) {
            when (it) {
                UiEvent.TriggerSync -> syncTracker.syncData(force = true)
            }
        }
    }

    private fun SyncTracker.syncData(force: Boolean = false) {
        @Suppress("DeferredResultUnused")
        syncService.syncFollowupsAsync()
        @Suppress("DeferredResultUnused")
        syncService.syncToolSharesAsync()

        launchSync {
            coroutineScope {
                launch { syncService.syncFavoriteTools(force) }
                launch { syncService.syncTools(force) }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navigator: Navigator, screen: DashboardScreen): DashboardPresenter
    }
}
