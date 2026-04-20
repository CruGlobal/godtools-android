package org.cru.godtools.base.ui.circuit.screen.dashboard.page

import com.slack.circuit.runtime.screen.Screen
import org.cru.godtools.base.ui.dashboard.Page

abstract class DashboardPage(val page: Page) : Screen {
    companion object {
        // TODO: Temporary to convert from Page to DashboardPage,
        //       eventually we should just use DashboardPage everywhere and remove Page
        fun forPage(page: Page): DashboardPage = when (page) {
            Page.HOME -> HomeScreen
            Page.FAVORITE_TOOLS -> AllFavoritesScreen
            Page.ALL_TOOLS -> ToolsScreen
            Page.LESSONS -> LessonsScreen
        }
    }
}
