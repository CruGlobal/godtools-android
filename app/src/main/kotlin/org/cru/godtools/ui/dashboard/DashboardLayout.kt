package org.cru.godtools.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
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
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.shared.analytics.AnalyticsScreenNames
import org.cru.godtools.ui.dashboard.home.AllFavoritesList
import org.cru.godtools.ui.dashboard.home.HomeContent
import org.cru.godtools.ui.dashboard.lessons.LessonsLayout
import org.cru.godtools.ui.dashboard.tools.ToolsLayout

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DashboardLayout(
    viewModel: DashboardViewModel = viewModel(),
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit,
    onOpenToolDetails: (String) -> Unit
) {
    val currentPage by viewModel.currentPage.collectAsState()
    DashboardLayoutAnalytics(currentPage)

    val hasBackStack by viewModel.hasBackStack.collectAsState()
    BackHandler(hasBackStack) { viewModel.popPageStack() }

    Scaffold(
        bottomBar = { DashboardBottomNavBar(currentPage, onSelectPage = { viewModel.updateCurrentPage(it) }) }
    ) {
        val saveableStateHolder = rememberSaveableStateHolder()
        SwipeRefresh(
            rememberSwipeRefreshState(viewModel.isSyncRunning.collectAsState().value),
            onRefresh = { viewModel.triggerSync(true) },
            modifier = Modifier.padding(it)
        ) {
            Crossfade(currentPage) { page ->
                saveableStateHolder.SaveableStateProvider(page) {
                    when (page) {
                        Page.LESSONS -> LessonsLayout(
                            onOpenLesson = { tool, translation -> onOpenTool(tool, translation, null) }
                        )
                        Page.HOME -> HomeContent(
                            onOpenTool = onOpenTool,
                            onOpenToolDetails = onOpenToolDetails,
                            onViewAllFavorites = {
                                saveableStateHolder.removeState(Page.FAVORITE_TOOLS)
                                viewModel.updateCurrentPage(Page.FAVORITE_TOOLS, false)
                            },
                            onViewAllTools = { viewModel.updateCurrentPage(Page.ALL_TOOLS) }
                        )
                        Page.FAVORITE_TOOLS -> AllFavoritesList(
                            onOpenTool = onOpenTool,
                            onOpenToolDetails = onOpenToolDetails
                        )
                        Page.ALL_TOOLS -> ToolsLayout(
                            onToolClicked = onOpenToolDetails
                        )
                    }
                }
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
