package org.cru.godtools.base.ui.circuit

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.runtime.screen.Screen
import kotlin.test.Test
import kotlin.test.assertEquals
import org.cru.godtools.base.ui.circuit.CircuitActivity.Companion.EXTRA_SCREEN
import org.cru.godtools.base.ui.circuit.screen.dashboard.DashboardScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val SUPPORTED_HOST = "supported.example.com"

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class CircuitActivityTest {
    private val deepLinkParser: CircuitDeepLinkParser = object : CircuitDeepLinkParser {
        override fun isDeepLinkSupported(uri: Uri) = uri.host == SUPPORTED_HOST

        override fun parseDeepLink(uri: Uri): List<Screen> = listOf(DashboardScreen(ToolsScreen))
    }
    private val deepLinkParsers = setOf(deepLinkParser)

    // region resolveInitialScreen()
    @Test
    fun `resolveInitialScreen() - Supported deep link`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$SUPPORTED_HOST/path"))
        assertEquals(listOf(DashboardScreen(ToolsScreen)), intent.resolveInitialScreen(deepLinkParsers))
    }

    @Test
    fun `resolveInitialScreen() - Screen extra`() {
        val intent = Intent().putExtra(EXTRA_SCREEN, DashboardScreen(LessonsScreen))
        assertEquals(listOf(DashboardScreen(LessonsScreen)), intent.resolveInitialScreen(deepLinkParsers))
    }

    @Test
    fun `resolveInitialScreen() - Fallback - No deep link or screen extra`() {
        assertEquals(listOf(DashboardScreen()), Intent().resolveInitialScreen(deepLinkParsers))
    }

    @Test
    fun `resolveInitialScreen() - Fallback - Unsupported deep link`() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://unsupported.example.com/path"))
        assertEquals(listOf(DashboardScreen()), intent.resolveInitialScreen(deepLinkParsers))
    }
    // endregion resolveInitialScreen()
}
