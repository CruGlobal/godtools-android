package org.cru.godtools.ui.dashboard

import android.app.Application
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.presenterOf
import com.slack.circuit.runtime.ui.ui
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.DashboardPage
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.base.ui.compose.LocalEventBus
import org.cru.godtools.ui.dashboard.DashboardPresenter.UiState
import org.cru.godtools.ui.dashboard.personalization.DashboardPersonalizationState
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class DashboardLayoutTest {
    private object EmptyState : CircuitUiState

    private val circuit = Circuit.Builder()
        .apply {
            listOf(HomeScreen, ToolsScreen, LessonsScreen).forEach { page ->
                addPresenterFactory { screen, _, _ -> if (screen == page) presenterOf { EmptyState } else null }
                addUiFactory { screen, _ ->
                    if (screen == page) ui<EmptyState> { _, _ -> } else null
                }
            }
        }
        .build()

    private fun ComposeUiTest.setDashboard(page: DashboardPage, personalization: DashboardPersonalizationState) =
        setContent {
            CircuitCompositionLocals(circuit) {
                CompositionLocalProvider(
                    // mocks required for AppUpdateSnackbar
                    LocalActivityResultRegistryOwner provides mockk<ActivityResultRegistryOwner>(relaxed = true),
                    LocalAppUpdateManager provides mockk(relaxed = true),
                    LocalEventBus provides mockk(relaxed = true),
                ) {
                    DashboardLayout(UiState(initialPage = page, personalization = personalization))
                }
            }
        }

    // region App bar personalization toggle
    @Test
    fun `AppBar - toggle shown on Tools when personalization is enabled`() = runComposeUiTest {
        setDashboard(ToolsScreen, DashboardPersonalizationState(isEnabled = true))

        onNodeWithText("All Tools").assertExists()
    }

    @Test
    fun `AppBar - toggle shown on Lessons when personalization is enabled`() = runComposeUiTest {
        setDashboard(LessonsScreen, DashboardPersonalizationState(isEnabled = true))

        onNodeWithText("All Lessons").assertExists()
    }

    @Test
    fun `AppBar - toggle not shown when personalization is disabled`() = runComposeUiTest {
        setDashboard(ToolsScreen, DashboardPersonalizationState(isEnabled = false))

        onNodeWithText("All Tools").assertDoesNotExist()
    }

    @Test
    fun `AppBar - toggle not shown on Home`() = runComposeUiTest {
        setDashboard(HomeScreen, DashboardPersonalizationState(isEnabled = true))

        onNodeWithText("Personalized").assertDoesNotExist()
    }

    @Test
    fun `AppBar - Tools toggle updates the Tools state only`() = runComposeUiTest {
        val personalization = DashboardPersonalizationState(isEnabled = true)
        setDashboard(ToolsScreen, personalization)

        onNodeWithText("All Tools").performClick()
        assertFalse(personalization.toolsPersonalized)
        assertTrue(personalization.lessonsPersonalized)
    }

    @Test
    fun `AppBar - Lessons toggle updates the Lessons state only`() = runComposeUiTest {
        val personalization = DashboardPersonalizationState(isEnabled = true)
        setDashboard(LessonsScreen, personalization)

        onNodeWithText("All Lessons").performClick()
        assertFalse(personalization.lessonsPersonalized)
        assertTrue(personalization.toolsPersonalized)
    }
    // endregion App bar personalization toggle
}
