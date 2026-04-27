package org.cru.godtools.ui.dashboard

import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuitx.navigation.intercepting.InterceptedResult
import com.slack.circuitx.navigation.intercepting.NavigationContext
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor
import com.slack.circuitx.navigation.intercepting.NavigationInterceptor.Companion.SuccessConsumed
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.DashboardPage

internal class DashboardPageNavigationInterceptor(val pageNavigator: Navigator) : NavigationInterceptor {
    override fun goTo(screen: Screen, navigationContext: NavigationContext) = when (screen) {
        is DashboardPage -> {
            pageNavigator.goTo(screen)
            SuccessConsumed
        }

        else -> super.goTo(screen, navigationContext)
    }

    override fun pop(result: PopResult?, navigationContext: NavigationContext): InterceptedResult {
        pageNavigator.pop(result)
        return SuccessConsumed
    }

    override fun resetRoot(newRoot: Screen, options: Navigator.StateOptions, navigationContext: NavigationContext) =
        when (newRoot) {
            is DashboardPage -> {
                pageNavigator.resetRoot(newRoot, options)
                SuccessConsumed
            }

            else -> super.resetRoot(newRoot, options, navigationContext)
        }
}
