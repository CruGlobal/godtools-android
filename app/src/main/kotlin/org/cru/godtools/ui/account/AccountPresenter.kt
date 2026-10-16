package org.cru.godtools.ui.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.sync.SyncTracker
import org.ccci.gto.android.common.sync.rememberSyncTask
import org.ccci.gto.android.common.sync.rememberSyncTaskRegistry
import org.cru.godtools.base.CONFIG_UI_GLOBAL_ACTIVITY_ENABLED
import org.cru.godtools.db.repository.GlobalActivityRepository
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.model.User
import org.cru.godtools.shared.user.activity.model.UserActivity
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.account.AccountPresenter.UiState
import org.cru.godtools.ui.account.globalactivity.GlobalActivityScreen
import org.cru.godtools.ui.drawer.DrawerMenuPresenter
import org.cru.godtools.ui.drawer.DrawerMenuScreen
import org.cru.godtools.user.activity.UserActivityManager
import org.cru.godtools.user.data.UserManager

class AccountPresenter @AssistedInject internal constructor(
    private val remoteConfig: FirebaseRemoteConfig,
    private val globalActivityRepository: GlobalActivityRepository,
    private val syncService: GodToolsSyncService,
    private val userActivityManager: UserActivityManager,
    private val userManager: UserManager,
    private val drawerMenuPresenter: DrawerMenuPresenter,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    @ConsistentCopyVisibility
    data class UiState internal constructor(
        val user: User? = null,
        val pages: ImmutableList<AccountPage> = persistentListOf(),
        val userActivity: UserActivity = UserActivity(emptyMap()),
        val globalActivity: GlobalActivityScreen.UiState = GlobalActivityScreen.UiState(
            activity = GlobalActivityAnalytics()
        ),
        val isSyncRunning: Boolean = false,
        val drawerState: DrawerMenuScreen.State = DrawerMenuScreen.State(),
        internal val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    internal sealed interface UiEvent : CircuitUiEvent {
        data object NavigateUp : UiEvent
        data object TriggerSync : UiEvent
    }

    @Composable
    override fun present(): UiState {
        val syncRegistry = rememberSyncTaskRegistry()
        syncRegistry.rememberSyncTask { syncData(it) }

        return UiState(
            user = remember { userManager.userFlow }.collectAsState(null).value,
            pages = remember {
                buildList {
                    add(AccountPage.ACTIVITY)
                    if (remoteConfig.getBoolean(CONFIG_UI_GLOBAL_ACTIVITY_ENABLED)) add(AccountPage.GLOBAL_ACTIVITY)
                }.toImmutableList()
            },
            userActivity = remember { userActivityManager.userActivityFlow }
                .collectAsState(UserActivity(emptyMap())).value,
            globalActivity = GlobalActivityScreen.UiState(
                activity = remember { globalActivityRepository.getGlobalActivityFlow() }
                    .collectAsState(GlobalActivityAnalytics()).value
            ),
            isSyncRunning = syncRegistry.syncTracker.isSyncing.collectAsState().value,
            drawerState = drawerMenuPresenter.present(),
        ) {
            when (it) {
                UiEvent.NavigateUp -> navigator.pop()
                UiEvent.TriggerSync -> syncRegistry.triggerSyncTasks(force = true)
            }
        }
    }

    private fun SyncTracker.syncData(force: Boolean = false) = launchSync {
        coroutineScope {
            launch { syncService.syncUser(force) }
            launch { syncService.syncUserCounters(force) }
            launch { syncService.syncGlobalActivity(force) }
        }
    }

    @AssistedFactory
    @CircuitInject(AccountScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): AccountPresenter
    }
}
