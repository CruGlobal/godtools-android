package org.cru.godtools.ui.dashboard

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slack.circuit.backstack.isAtRoot
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuit.runtime.navigation.currentScreen
import com.slack.circuit.runtime.resetRoot
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.android.IntentScreen
import com.slack.circuitx.navigation.intercepting.InterceptedResult
import com.slack.circuitx.navigation.intercepting.NavigationContext
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor.Companion.Skipped
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor.Companion.SuccessConsumed
import com.slack.circuitx.navigation.intercepting.rememberInterceptingNavigator
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.material3.ui.navigationdrawer.toggle
import org.cru.godtools.R
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_ALL_TOOLS
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_HOME
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_LESSONS
import org.cru.godtools.analytics.firebase.model.FirebaseIamActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.AllFavoritesScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.DashboardPage
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.base.ui.compose.LocalEventBus
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.shared.analytics.AnalyticsScreenNames
import org.cru.godtools.ui.dashboard.DashboardPresenter.UiEvent
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationModalOverlay
import org.cru.godtools.ui.dashboard.optinnotification.PermissionStatus
import org.cru.godtools.ui.drawer.DrawerMenuLayout
import org.cru.godtools.ui.drawer.DrawerViewModel

internal sealed interface DashboardEvent {
    class OpenIntent(val intent: Intent) : DashboardEvent
    class OpenScreen(val screen: Screen) : DashboardEvent
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DashboardLayout(
    requestPermission: suspend () -> Unit,
    onEvent: (DashboardEvent) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    // region optInNotification
    val showOverlay by viewModel.showOptInNotification.collectAsState()

    if (showOverlay) {
        OverlayEffect {
            show(
                OptInNotificationModalOverlay(
                    requestPermission = requestPermission,
                    isHardDenied = viewModel.permissionStatus == PermissionStatus.HARD_DENIED
                )
            )
            viewModel.setShowOptInNotification(false)
        }
    }
    // endregion optInNotification

    val backStack = rememberSaveableBackStack(HomeScreen)
    val pageNavigator = rememberInterceptingNavigator(
        rememberCircuitNavigator(backStack),
        interceptors = remember(onEvent) {
            listOf(
                object : NavigationInterceptor {
                    override fun goTo(screen: Screen, navigationContext: NavigationContext): InterceptedResult {
                        if (screen is DashboardPage) return Skipped

                        when (screen) {
                            is IntentScreen -> onEvent(DashboardEvent.OpenIntent(screen.intent))
                            else -> onEvent(DashboardEvent.OpenScreen(screen))
                        }

                        return SuccessConsumed
                    }
                }
            )
        }
    )

    val state = DashboardPresenter.UiState(
        drawerState = viewModel<DrawerViewModel>().toState(),
        isSyncing = viewModel.isSyncRunning.collectAsState().value,
        pageBackStack = backStack,
        pageNavigator = pageNavigator,
        snackbarState = remember { SnackbarHostState() },
    ) {
        when (it) {
            UiEvent.TriggerSync -> viewModel.triggerSync(true)
        }
    }

    DashboardLayout(state)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DashboardLayout(state: DashboardPresenter.UiState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    val currentScreen = state.pageBackStack.currentScreen ?: HomeScreen

    AppUpdateSnackbar(state.snackbarState)
    DashboardLayoutAnalytics(state.pageBackStack.currentScreen ?: HomeScreen)

    DrawerMenuLayout(state.drawerState, modifier = modifier) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        when {
                            !state.pageBackStack.isAtRoot -> IconButton(onClick = { state.pageNavigator.pop() }) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                            }

                            else -> IconButton(onClick = { scope.launch { state.drawerState.drawerState.toggle() } }) {
                                Icon(Icons.Default.Menu, null)
                            }
                        }
                    },
                    colors = GodToolsTheme.topAppBarColors,
                )
            },
            bottomBar = {
                DashboardBottomNavBar(
                    currentScreen,
                    onSelectPage = {
                        state.pageNavigator.resetRoot(it, saveState = true, restoreState = it != HomeScreen)
                    }
                )
            },
            snackbarHost = { SnackbarHost(state.snackbarState) }
        ) {
            PullToRefreshBox(
                state.isSyncing,
                onRefresh = { state.eventSink(UiEvent.TriggerSync) },
                modifier = Modifier.padding(it)
            ) {
                NavigableCircuitContent(
                    navigator = state.pageNavigator,
                    backStack = state.pageBackStack,
                )
            }
        }
    }
}

@Composable
private fun DashboardLayoutAnalytics(screen: Screen) {
    val eventBus = LocalEventBus.current
    when (screen) {
        is LessonsScreen -> {
            RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenNames.DASHBOARD_LESSONS))
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                eventBus.post(FirebaseIamActionEvent(ACTION_IAM_LESSONS))
            }
        }

        is HomeScreen, is AllFavoritesScreen -> {
            RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenNames.DASHBOARD_HOME))
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                eventBus.post(FirebaseIamActionEvent(ACTION_IAM_HOME))
            }
        }

        is ToolsScreen -> {
            RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenNames.DASHBOARD_ALL_TOOLS))
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                eventBus.post(FirebaseIamActionEvent(ACTION_IAM_ALL_TOOLS))
            }
        }
    }
}

@Composable
private fun DashboardBottomNavBar(currentScreen: Screen, onSelectPage: (DashboardPage) -> Unit) {
    NavigationBar(modifier = Modifier.shadow(8.dp, clip = false)) {
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_lessons), stringResource(R.string.nav_lessons)) },
            label = { Text(stringResource(R.string.nav_lessons)) },
            selected = currentScreen == LessonsScreen,
            onClick = { onSelectPage(LessonsScreen) },
        )

        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_favorite_24dp), stringResource(R.string.nav_favorite_tools)) },
            label = { Text(stringResource(R.string.nav_favorite_tools)) },
            selected = currentScreen == HomeScreen || currentScreen == AllFavoritesScreen,
            onClick = { onSelectPage(HomeScreen) },
        )

        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_all_tools), stringResource(R.string.nav_all_tools)) },
            label = { Text(stringResource(R.string.nav_all_tools)) },
            selected = currentScreen == ToolsScreen,
            onClick = { onSelectPage(ToolsScreen) },
        )
    }
}
