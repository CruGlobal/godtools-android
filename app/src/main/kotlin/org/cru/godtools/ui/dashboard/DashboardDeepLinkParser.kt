package org.cru.godtools.ui.dashboard

import android.net.Uri
import com.slack.circuit.runtime.screen.Screen
import org.cru.godtools.BuildConfig.HOST_GODTOOLS_CUSTOM_URI
import org.cru.godtools.base.HOST_DYNALINKS
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.SCHEME_GODTOOLS
import org.cru.godtools.base.ui.circuit.CircuitDeepLinkParser
import org.cru.godtools.base.ui.circuit.screen.dashboard.DashboardScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.DashboardPage
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen

object DashboardDeepLinkParser : CircuitDeepLinkParser {
    override fun isDeepLinkSupported(uri: Uri) = when {
        uri.isDashboardCustomUriSchemeDeepLink() -> true
        uri.isDashboardDynalinksDeepLink() -> true
        uri.isDashboardGodToolsDeepLink() -> true
        uri.isDashboardLessonsDeepLink() -> true
        else -> false
    }

    override fun parseDeepLink(uri: Uri): List<Screen> {
        val page = when {
            uri.isDashboardCustomUriSchemeDeepLink() -> findPageByUriPathSegment(uri.pathSegments.getOrNull(1))
            uri.isDashboardDynalinksDeepLink() -> findPageByUriPathSegment(uri.pathSegments.getOrNull(2))
            uri.isDashboardGodToolsDeepLink() -> findPageByUriPathSegment(uri.pathSegments.getOrNull(2))
            uri.isDashboardLessonsDeepLink() -> LessonsScreen
            else -> HomeScreen
        }

        return listOf(DashboardScreen(page))
    }

    private fun Uri.isDashboardCustomUriSchemeDeepLink() = SCHEME_GODTOOLS.equals(scheme, true) &&
        HOST_GODTOOLS_CUSTOM_URI.equals(host, true) &&
        pathSegments.getOrNull(0) == "dashboard"

    private fun Uri.isDashboardDynalinksDeepLink() = (scheme == "http" || scheme == "https") &&
        host.equals(HOST_DYNALINKS, true) &&
        pathSegments.getOrNull(0) == "deeplink" &&
        pathSegments.getOrNull(1) == "dashboard"

    private fun Uri.isDashboardGodToolsDeepLink() = (scheme == "http" || scheme == "https") &&
        host.equals(HOST_GODTOOLSAPP_COM, true) &&
        pathSegments.getOrNull(0) == "deeplink" &&
        pathSegments.getOrNull(1) == "dashboard"

    private fun Uri.isDashboardLessonsDeepLink() = (scheme == "http" || scheme == "https") &&
        host.equals(HOST_GODTOOLSAPP_COM, true) &&
        path == "/lessons"

    private fun findPageByUriPathSegment(segment: String?): DashboardPage = when (segment) {
        "lessons" -> LessonsScreen
        "tools" -> ToolsScreen
        "home" -> HomeScreen
        else -> HomeScreen
    }
}
