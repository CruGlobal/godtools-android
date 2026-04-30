package org.cru.godtools.ui.dashboard

import android.app.Application
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.cru.godtools.BuildConfig.HOST_GODTOOLS_CUSTOM_URI
import org.cru.godtools.base.HOST_DYNALINKS
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.ui.circuit.screen.dashboard.DashboardScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.ui.dashboard.DashboardDeepLinkParser.isDeepLinkSupported
import org.cru.godtools.ui.dashboard.DashboardDeepLinkParser.parseDeepLink
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DashboardDeepLinkParserTest {
    // region isDeepLinkSupported()
    @Test
    fun `isDeepLinkSupported() - Custom URI Scheme`() {
        assertTrue(isDeepLinkSupported(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard")))
        assertTrue(isDeepLinkSupported(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard/home")))
        assertTrue(isDeepLinkSupported(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard/lessons")))
        assertTrue(isDeepLinkSupported(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard/tools")))
    }

    @Test
    fun `isDeepLinkSupported() - godtoolsapp_com deeplink`() {
        assertTrue(isDeepLinkSupported(Uri.parse("http://$HOST_GODTOOLSAPP_COM/deeplink/dashboard")))
        assertTrue(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard")))
        assertTrue(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard/home")))
        assertTrue(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard/lessons")))
        assertTrue(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard/tools")))
    }

    @Test
    fun `isDeepLinkSupported() - dynalinks deeplink`() {
        assertTrue(isDeepLinkSupported(Uri.parse("http://$HOST_DYNALINKS/deeplink/dashboard")))
        assertTrue(isDeepLinkSupported(Uri.parse("https://$HOST_DYNALINKS/deeplink/dashboard")))
        assertTrue(isDeepLinkSupported(Uri.parse("https://$HOST_DYNALINKS/deeplink/dashboard/home")))
        assertTrue(isDeepLinkSupported(Uri.parse("https://$HOST_DYNALINKS/deeplink/dashboard/lessons")))
        assertTrue(isDeepLinkSupported(Uri.parse("https://$HOST_DYNALINKS/deeplink/dashboard/tools")))
    }

    @Test
    fun `isDeepLinkSupported() - legacy lessons`() {
        assertTrue(isDeepLinkSupported(Uri.parse("http://$HOST_GODTOOLSAPP_COM/lessons")))
        assertTrue(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLSAPP_COM/lessons")))
    }

    @Test
    fun `isDeepLinkSupported() - Invalid`() {
        // wrong host
        assertFalse(isDeepLinkSupported(Uri.parse("https://example.com/deeplink/dashboard")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://example.com/lessons")))

        // wrong scheme for custom URI
        assertFalse(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLS_CUSTOM_URI/dashboard")))

        // wrong path for lessons
        assertFalse(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLSAPP_COM/lessons/lessonhs/en")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLSAPP_COM/lessons/")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLSAPP_COM/lesson")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://$HOST_DYNALINKS/lessons")))

        // insufficient path segments — would throw IndexOutOfBoundsException with direct pathSegments[n] access
        assertFalse(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLSAPP_COM")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://$HOST_DYNALINKS")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://$HOST_DYNALINKS/deeplink")))
    }
    // endregion isDeepLinkSupported()

    // region parseDeepLink()
    @Test
    fun `parseDeepLink() - Custom URI Scheme - defaults to HomeScreen`() {
        assertEquals(
            listOf(DashboardScreen(HomeScreen)),
            parseDeepLink(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard"))
        )
        assertEquals(
            listOf(DashboardScreen(HomeScreen)),
            parseDeepLink(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard/home"))
        )
        assertEquals(
            listOf(DashboardScreen(HomeScreen)),
            parseDeepLink(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard/unknown"))
        )
    }

    @Test
    fun `parseDeepLink() - Custom URI Scheme - LessonsScreen`() {
        assertEquals(
            listOf(DashboardScreen(LessonsScreen)),
            parseDeepLink(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard/lessons"))
        )
    }

    @Test
    fun `parseDeepLink() - Custom URI Scheme - ToolsScreen`() {
        assertEquals(
            listOf(DashboardScreen(ToolsScreen)),
            parseDeepLink(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard/tools"))
        )
    }

    @Test
    fun `parseDeepLink() - godtoolsapp_com - defaults to HomeScreen`() {
        assertEquals(
            listOf(DashboardScreen(HomeScreen)),
            parseDeepLink(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard"))
        )
        assertEquals(
            listOf(DashboardScreen(HomeScreen)),
            parseDeepLink(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard/home"))
        )
        assertEquals(
            listOf(DashboardScreen(HomeScreen)),
            parseDeepLink(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard/unknown"))
        )
    }

    @Test
    fun `parseDeepLink() - godtoolsapp_com - LessonsScreen`() {
        assertEquals(
            listOf(DashboardScreen(LessonsScreen)),
            parseDeepLink(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard/lessons"))
        )
    }

    @Test
    fun `parseDeepLink() - godtoolsapp_com - ToolsScreen`() {
        assertEquals(
            listOf(DashboardScreen(ToolsScreen)),
            parseDeepLink(Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard/tools"))
        )
    }

    @Test
    fun `parseDeepLink() - dynalinks - defaults to HomeScreen`() {
        assertEquals(
            listOf(DashboardScreen(HomeScreen)),
            parseDeepLink(Uri.parse("https://$HOST_DYNALINKS/deeplink/dashboard"))
        )
        assertEquals(
            listOf(DashboardScreen(HomeScreen)),
            parseDeepLink(Uri.parse("https://$HOST_DYNALINKS/deeplink/dashboard/home"))
        )
        assertEquals(
            listOf(DashboardScreen(HomeScreen)),
            parseDeepLink(Uri.parse("https://$HOST_DYNALINKS/deeplink/dashboard/unknown"))
        )
    }

    @Test
    fun `parseDeepLink() - dynalinks - LessonsScreen`() {
        assertEquals(
            listOf(DashboardScreen(LessonsScreen)),
            parseDeepLink(Uri.parse("https://$HOST_DYNALINKS/deeplink/dashboard/lessons"))
        )
    }

    @Test
    fun `parseDeepLink() - dynalinks - ToolsScreen`() {
        assertEquals(
            listOf(DashboardScreen(ToolsScreen)),
            parseDeepLink(Uri.parse("https://$HOST_DYNALINKS/deeplink/dashboard/tools"))
        )
    }

    @Test
    fun `parseDeepLink() - legacy lessons`() {
        assertEquals(
            listOf(DashboardScreen(LessonsScreen)),
            parseDeepLink(Uri.parse("https://$HOST_GODTOOLSAPP_COM/lessons"))
        )
    }
    // endregion parseDeepLink()
}
