package org.cru.godtools.ui.dashboard

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
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
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.overlay.OverlayEffect
import com.slack.circuitx.android.IntentScreen
import java.util.Locale
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.material3.ui.navigationdrawer.toggle
import org.cru.godtools.R
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_ALL_TOOLS
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_HOME
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_LESSONS
import org.cru.godtools.analytics.firebase.model.FirebaseIamActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.ui.compose.LocalEventBus
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.model.Tool
import org.cru.godtools.shared.analytics.AnalyticsScreenNames
import org.cru.godtools.ui.dashboard.home.AllFavoritesScreen
import org.cru.godtools.ui.dashboard.home.HomeScreen
import org.cru.godtools.ui.dashboard.lessons.LessonsScreen
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationModalOverlay
import org.cru.godtools.ui.dashboard.optinnotification.PermissionStatus
import org.cru.godtools.ui.dashboard.tools.ToolsScreen
import org.cru.godtools.ui.drawer.DrawerMenuLayout
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen

internal sealed interface DashboardEvent {
    class OpenIntent(val intent: Intent) : DashboardEvent
    open class OpenTool(val tool: String?, val type: Tool.Type?, val lang1: Locale?, val lang2: Locale? = null) :
        DashboardEvent
    class OpenToolDetails(val tool: String?, val lang: Locale? = null) : DashboardEvent
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DashboardLayout(
    requestPermission: suspend () -> Unit,
    onEvent: (DashboardEvent) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val currentPage by viewModel.currentPage.collectAsState()
    DashboardLayoutAnalytics(currentPage)

    val hasBackStack by viewModel.hasBackStack.collectAsState()
    BackHandler(hasBackStack) { viewModel.popPageStack() }

    val refreshing by viewModel.isSyncRunning.collectAsState()
    val refreshState = rememberPullToRefreshState()

    val snackbarHostState = remember { SnackbarHostState() }
    AppUpdateSnackbar(snackbarHostState)

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

    DrawerMenuLayout(drawerState = drawerState) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        when {
                            hasBackStack -> IconButton(onClick = { viewModel.popPageStack() }) {
                                Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                            }
                            else -> IconButton(onClick = { scope.launch { drawerState.toggle() } }) {
                                Icon(Icons.Default.Menu, null)
                            }
                        }
                    },
                    colors = GodToolsTheme.topAppBarColors,
                )
            },
            bottomBar = { DashboardBottomNavBar(currentPage, onSelectPage = { viewModel.updateCurrentPage(it) }) },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) {
            val saveableStateHolder = rememberSaveableStateHolder()
            Box(
                modifier = Modifier
                    .padding(it)
                    .pullToRefresh(refreshing, state = refreshState, onRefresh = { viewModel.triggerSync(true) })
            ) {
                Crossfade(currentPage, label = "Main Content Crossfade") { page ->
                    saveableStateHolder.SaveableStateProvider(page) {
                        when (page) {
                            Page.LESSONS,
                            Page.HOME,
                            Page.FAVORITE_TOOLS,
                            Page.ALL_TOOLS -> {
                                CircuitContent(
                                    screen = when (page) {
                                        Page.LESSONS -> LessonsScreen
                                        Page.HOME -> HomeScreen
                                        Page.FAVORITE_TOOLS -> AllFavoritesScreen
                                        Page.ALL_TOOLS -> ToolsScreen
                                        else -> error("Page $page is not converted to Circuit yet")
                                    },
                                    onNavEvent = {
                                        when (it) {
                                            is NavEvent.GoTo -> when (val screen = it.screen) {
                                                AllFavoritesScreen -> {
                                                    saveableStateHolder.removeState(Page.FAVORITE_TOOLS)
                                                    viewModel.updateCurrentPage(Page.FAVORITE_TOOLS, false)
                                                }
                                                is IntentScreen -> onEvent(DashboardEvent.OpenIntent(screen.intent))
                                                is ToolDetailsScreen -> onEvent(
                                                    DashboardEvent.OpenToolDetails(
                                                        screen.initialTool,
                                                        screen.secondLanguage,
                                                    )
                                                )
                                            }
                                            is NavEvent.ResetRoot -> when (it.newRoot) {
                                                ToolsScreen -> viewModel.updateCurrentPage(Page.ALL_TOOLS)
                                            }
                                            else -> Unit
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                PullToRefreshDefaults.Indicator(
                    state = refreshState,
                    isRefreshing = refreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

@Composable
private fun DashboardLayoutAnalytics(page: Page) {
    val eventBus = LocalEventBus.current
    when (page) {
        Page.LESSONS -> {
            RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenNames.DASHBOARD_LESSONS))
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                eventBus.post(FirebaseIamActionEvent(ACTION_IAM_LESSONS))
            }
        }
        Page.HOME, Page.FAVORITE_TOOLS -> {
            RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenNames.DASHBOARD_HOME))
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                eventBus.post(FirebaseIamActionEvent(ACTION_IAM_HOME))
            }
        }
        Page.ALL_TOOLS -> {
            RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenNames.DASHBOARD_ALL_TOOLS))
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
                eventBus.post(FirebaseIamActionEvent(ACTION_IAM_ALL_TOOLS))
            }
        }
    }
}

@Composable
private fun DashboardBottomNavBar(currentPage: Page, onSelectPage: (Page) -> Unit) {
    NavigationBar(modifier = Modifier.shadow(8.dp, clip = false)) {
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_lessons), stringResource(R.string.nav_lessons)) },
            label = { Text(stringResource(R.string.nav_lessons)) },
            selected = currentPage == Page.LESSONS,
            onClick = { onSelectPage(Page.LESSONS) },
        )

        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_favorite_24dp), stringResource(R.string.nav_favorite_tools)) },
            label = { Text(stringResource(R.string.nav_favorite_tools)) },
            selected = currentPage == Page.HOME || currentPage == Page.FAVORITE_TOOLS,
            onClick = { onSelectPage(Page.HOME) },
        )

        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_all_tools), stringResource(R.string.nav_all_tools)) },
            label = { Text(stringResource(R.string.nav_all_tools)) },
            selected = currentPage == Page.ALL_TOOLS,
            onClick = { onSelectPage(Page.ALL_TOOLS) },
        )
    }
}
