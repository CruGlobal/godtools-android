package org.cru.godtools.ui.dashboard

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.slack.circuit.backstack.BackStack
import com.slack.circuit.backstack.isAtRoot
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.navigation.currentScreen
import com.slack.circuit.runtime.resetRoot
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.navigation.intercepting.InterceptedResult
import com.slack.circuitx.navigation.intercepting.NavigationContext
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor.Companion.SuccessConsumed
import com.slack.circuitx.navigation.intercepting.rememberInterceptingNavigator
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.material3.ui.navigationdrawer.toggle
import org.cru.godtools.R
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_ALL_TOOLS
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_HOME
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_LESSONS
import org.cru.godtools.analytics.firebase.model.FirebaseIamActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.ui.circuit.screen.dashboard.DashboardScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.AllFavoritesScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.DashboardPage
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.base.ui.compose.LocalEventBus
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.shared.analytics.AnalyticsScreenNames
import org.cru.godtools.ui.dashboard.DashboardPresenter.UiEvent
import org.cru.godtools.ui.dashboard.DashboardPresenter.UiState
import org.cru.godtools.ui.drawer.DrawerMenuLayout

@Composable
@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(DashboardScreen::class, SingletonComponent::class)
internal fun DashboardLayout(state: UiState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    val backStack = rememberSaveableBackStack(state.initialPage)
    val navigator = rememberDashboardPageNavigator(backStack, state.eventSink)
    val currentScreen = backStack.currentScreen ?: HomeScreen

    AppUpdateSnackbar(state.snackbarState)
    DashboardLayoutAnalytics(currentScreen)

    DrawerMenuLayout(state.drawerState, modifier = modifier) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        when {
                            !backStack.isAtRoot -> IconButton(onClick = { navigator.pop() }) {
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
                    onSelectPage = { navigator.resetRoot(it, saveState = true, restoreState = it != HomeScreen) }
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
                    navigator = navigator,
                    backStack = backStack,
                )
            }
        }
    }
}

@Composable
private fun rememberDashboardPageNavigator(
    backStack: BackStack<out BackStack.Record>,
    eventSink: (UiEvent) -> Unit,
): Navigator = rememberInterceptingNavigator(
    rememberCircuitNavigator(backStack) { result ->
        eventSink(UiEvent.NestedNavEvent(NavEvent.Pop(result)))
    },
    interceptors = listOf(
        object : NavigationInterceptor {
            override fun goTo(screen: Screen, navigationContext: NavigationContext) = when (screen) {
                is DashboardPage -> InterceptedResult.Skipped

                else -> {
                    eventSink(UiEvent.NestedNavEvent(NavEvent.GoTo(screen)))
                    SuccessConsumed
                }
            }

            override fun resetRoot(
                newRoot: Screen,
                options: Navigator.StateOptions,
                navigationContext: NavigationContext,
            ) = when (newRoot) {
                is DashboardPage -> InterceptedResult.Skipped

                else -> {
                    eventSink(UiEvent.NestedNavEvent(NavEvent.ResetRoot(newRoot, options)))
                    SuccessConsumed
                }
            }
        }
    )
)

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
