package org.cru.godtools.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.ccci.gto.android.common.androidx.lifecycle.compose.OnResume
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
import org.cru.godtools.ui.dashboard.home.AllFavoritesList
import org.cru.godtools.ui.dashboard.home.HomeContent
import org.cru.godtools.ui.dashboard.lessons.LessonsLayout
import org.cru.godtools.ui.dashboard.tools.ToolsLayout

@Composable
internal fun DashboardLayout(
    viewModel: DashboardViewModel = viewModel(),
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit,
    onOpenToolDetails: (String) -> Unit
) {
    val currentPage by viewModel.currentPage.collectAsState()
    DashboardLayoutAnalytics(currentPage)

    val hasBackStack by viewModel.hasBackStack.collectAsState()
    BackHandler(hasBackStack) { viewModel.popPageStack() }

    val saveableStateHolder = rememberSaveableStateHolder()
    SwipeRefresh(
        rememberSwipeRefreshState(viewModel.isSyncRunning.collectAsState().value),
        onRefresh = { viewModel.triggerSync(true) }
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

@Composable
private fun DashboardLayoutAnalytics(page: Page) {
    val eventBus = LocalEventBus.current
    when (page) {
        Page.LESSONS -> {
            RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenEvent.SCREEN_LESSONS))
            OnResume { eventBus.post(FirebaseIamActionEvent(ACTION_IAM_LESSONS)) }
        }
        Page.HOME, Page.FAVORITE_TOOLS -> {
            RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenEvent.SCREEN_HOME))
            OnResume { eventBus.post(FirebaseIamActionEvent(ACTION_IAM_HOME)) }
        }
        Page.ALL_TOOLS -> {
            RecordAnalyticsScreen(AnalyticsScreenEvent(AnalyticsScreenEvent.SCREEN_ALL_TOOLS))
            OnResume { eventBus.post(FirebaseIamActionEvent(ACTION_IAM_ALL_TOOLS)) }
        }
    }
}
