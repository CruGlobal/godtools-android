package org.cru.godtools.base.ui.circuit.screen.dashboard

import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.DashboardPage
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen

@Parcelize
data class DashboardScreen(val initialPage: DashboardPage = HomeScreen) : Screen
