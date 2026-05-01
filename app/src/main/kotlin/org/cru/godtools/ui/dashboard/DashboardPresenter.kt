package org.cru.godtools.ui.dashboard

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.sync.SyncTracker
import org.cru.godtools.base.ui.circuit.screen.dashboard.DashboardScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.DashboardPage
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.dashboard.DashboardPresenter.UiState
import org.cru.godtools.ui.dashboard.SyncTaskRegistry.Companion.rememberSyncRegistry
import org.cru.godtools.ui.dashboard.SyncTaskRegistry.Companion.syncTaskRegistry
import org.cru.godtools.ui.drawer.DrawerMenuPresenter
import org.cru.godtools.ui.drawer.DrawerMenuScreen

class DashboardPresenter @AssistedInject internal constructor(
    private val drawerMenuPresenter: DrawerMenuPresenter,
    private val syncService: GodToolsSyncService,
    @Assisted private val circuitContext: CircuitContext,
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: DashboardScreen,
) : Presenter<UiState> {
    @ConsistentCopyVisibility
    data class UiState internal constructor(
        val drawerState: DrawerMenuScreen.State = DrawerMenuScreen.State(),
        val isSyncing: Boolean = false,
        val initialPage: DashboardPage = HomeScreen,
        val snackbarState: SnackbarHostState = SnackbarHostState(),
        internal val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    internal sealed interface UiEvent : CircuitUiEvent {
        data object TriggerSync : UiEvent
        data class NestedNavEvent(val event: NavEvent) : UiEvent
    }

    @Composable
    override fun present(): UiState {
        val syncRegistry = rememberSyncRegistry()
        DisposableEffect(syncRegistry) {
            circuitContext.syncTaskRegistry = syncRegistry
            val id = syncRegistry.registerSyncTask { syncData(it) }

            onDispose {
                circuitContext.syncTaskRegistry = null
                syncRegistry.unregisterSyncTask(id)
            }
        }

        return UiState(
            drawerState = drawerMenuPresenter.present(),
            isSyncing = syncRegistry.syncTracker.isSyncing.collectAsState().value,
            initialPage = screen.initialPage,
            snackbarState = remember { SnackbarHostState() },
        ) {
            when (it) {
                UiEvent.TriggerSync -> syncRegistry.triggerSyncTasks(force = true)
                is UiEvent.NestedNavEvent -> navigator.onNavEvent(it.event)
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
    @CircuitInject(DashboardScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(context: CircuitContext, navigator: Navigator, screen: DashboardScreen): DashboardPresenter
    }
}
