package org.cru.godtools.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.material3.ui.navigationdrawer.toggle
import org.ccci.gto.android.common.androidx.compose.material3.ui.pullrefresh.PullRefreshIndicator
import org.ccci.gto.android.common.androidx.lifecycle.compose.OnResume
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
import org.cru.godtools.ui.dashboard.home.AllFavoritesList
import org.cru.godtools.ui.dashboard.home.DashboardHomeEvent
import org.cru.godtools.ui.dashboard.home.HomeContent
import org.cru.godtools.ui.dashboard.lessons.DashboardLessonsEvent
import org.cru.godtools.ui.dashboard.lessons.LessonsLayout
import org.cru.godtools.ui.dashboard.tools.ToolsLayout
import org.cru.godtools.ui.drawer.DrawerMenuLayout
import org.cru.godtools.ui.tools.ToolCardEvent

@Composable
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
internal fun DashboardLayout(
    viewModel: DashboardViewModel = viewModel(),
    onOpenTool: (Tool?, Locale?, Locale?) -> Unit,
    onOpenToolDetails: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val currentPage by viewModel.currentPage.collectAsState()
    DashboardLayoutAnalytics(currentPage)

    val hasBackStack by viewModel.hasBackStack.collectAsState()
    BackHandler(hasBackStack) { viewModel.popPageStack() }

    val refreshing by viewModel.isSyncRunning.collectAsState()
    val refreshState = rememberPullRefreshState(refreshing, onRefresh = { viewModel.triggerSync(true) })

    DrawerMenuLayout(drawerState) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        when {
                            hasBackStack -> IconButton(onClick = { viewModel.popPageStack() }) {
                                Icon(Icons.Default.ArrowBack, null)
                            }
                            else -> IconButton(onClick = { scope.launch { drawerState.toggle() } }) {
                                Icon(Icons.Default.Menu, null)
                            }
                        }
                    },
                    colors = GodToolsTheme.topAppBarColors,
                )
            },
            bottomBar = { DashboardBottomNavBar(currentPage, onSelectPage = { viewModel.updateCurrentPage(it) }) }
        ) {
            val saveableStateHolder = rememberSaveableStateHolder()
            Box(
                modifier = Modifier
                    .padding(it)
                    .pullRefresh(refreshState)
            ) {
                Crossfade(currentPage) { page ->
                    saveableStateHolder.SaveableStateProvider(page) {
                        when (page) {
                            Page.LESSONS -> LessonsLayout(
                                onEvent = {
                                    when (it) {
                                        is DashboardLessonsEvent.OpenLesson -> onOpenTool(it.tool, it.lang, null)
                                    }
                                },
                            )

                            Page.HOME -> HomeContent(
                                onEvent = {
                                    when (it) {
                                        is DashboardHomeEvent.OpenTool -> onOpenTool(it.tool, it.lang1, it.lang2)
                                        is DashboardHomeEvent.OpenToolDetails ->
                                            it.tool?.code?.let { onOpenToolDetails(it) }
                                        DashboardHomeEvent.ViewAllFavorites -> {
                                            saveableStateHolder.removeState(Page.FAVORITE_TOOLS)
                                            viewModel.updateCurrentPage(Page.FAVORITE_TOOLS, false)
                                        }
                                        DashboardHomeEvent.ViewAllTools -> viewModel.updateCurrentPage(Page.ALL_TOOLS)
                                    }
                                }
                            )

                            Page.FAVORITE_TOOLS -> AllFavoritesList(
                                onEvent = {
                                    when(it) {
                                        is ToolCardEvent.OpenTool,
                                        is ToolCardEvent.Click -> onOpenTool(it.tool, it.lang1, it.lang2)
                                        is ToolCardEvent.OpenToolDetails -> it.tool?.code?.let(onOpenToolDetails)
                                    }
                                },
                            )

                            Page.ALL_TOOLS -> ToolsLayout(
                                onToolClicked = onOpenToolDetails
                            )
                        }
                    }
                }

                PullRefreshIndicator(refreshing, refreshState, modifier = Modifier.align(Alignment.TopCenter))
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
            OnResume { eventBus.post(FirebaseIamActionEvent(ACTION_IAM_LESSONS)) }
        }
        Page.HOME, Page.FAVORITE_TOOLS -> {
            RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenNames.DASHBOARD_HOME))
            OnResume { eventBus.post(FirebaseIamActionEvent(ACTION_IAM_HOME)) }
        }
        Page.ALL_TOOLS -> {
            RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenNames.DASHBOARD_ALL_TOOLS))
            OnResume { eventBus.post(FirebaseIamActionEvent(ACTION_IAM_ALL_TOOLS)) }
        }
    }
}

@Composable
private fun DashboardBottomNavBar(currentPage: Page, onSelectPage: (Page) -> Unit) {
    NavigationBar(modifier = Modifier.shadow(8.dp, clip = false)) {
        val lessonsText = stringResource(R.string.nav_lessons)
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_lessons), lessonsText) },
            label = { Text(lessonsText) },
            selected = currentPage == Page.LESSONS,
            onClick = { onSelectPage(Page.LESSONS) },
        )

        val homeText = stringResource(R.string.nav_favorite_tools)
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_favorite_24dp), homeText) },
            label = { Text(homeText) },
            selected = currentPage == Page.HOME || currentPage == Page.FAVORITE_TOOLS,
            onClick = { onSelectPage(Page.HOME) },
        )

        val toolsText = stringResource(R.string.nav_all_tools)
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_all_tools), toolsText) },
            label = { Text(toolsText) },
            selected = currentPage == Page.ALL_TOOLS,
            onClick = { onSelectPage(Page.ALL_TOOLS) },
        )
    }
}
