package org.cru.godtools.ui.dashboard.optinnotification

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.Turbine
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import com.slack.circuitx.android.IntentScreen
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.ccci.gto.android.common.util.content.equalsIntent
import org.ccci.gto.support.turbine.awaitItemMatching
import org.cru.godtools.base.CONFIG_UI_OPT_IN_NOTIFICATION_ENABLED
import org.cru.godtools.base.CONFIG_UI_OPT_IN_NOTIFICATION_PROMPT_LIMIT
import org.cru.godtools.base.CONFIG_UI_OPT_IN_NOTIFICATION_TIME_INTERVAL
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_OPT_IN_NOTIFICATION
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.cru.godtools.base.ui.circuit.screen.dashboard.DashboardScreen
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationPresenter.UiEvent
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationPresenter.UiState
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DefaultOptInNotificationPresenterTest {
    private var isFeatureEnabled = true
    private var isOnboardingDiscovered = true
    private var lastPrompted = LocalDate.ofEpochDay(0)
    private var promptCount = 0

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val controller = FakeOptInNotificationController()
    private val remoteConfig: FirebaseRemoteConfig = mockk {
        every { getBoolean(CONFIG_UI_OPT_IN_NOTIFICATION_ENABLED) } answers { isFeatureEnabled }
        every { getLong(CONFIG_UI_OPT_IN_NOTIFICATION_PROMPT_LIMIT) } returns 5
        every { getLong(CONFIG_UI_OPT_IN_NOTIFICATION_TIME_INTERVAL) } returns 30
    }
    private val settings: Settings = mockk(relaxUnitFun = true) {
        every { isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING) } answers { isOnboardingDiscovered }
        every { getLastPromptedOptInNotification() } answers { lastPrompted }
        every { getOptInNotificationPromptCount() } answers { promptCount }
    }

    private val navigator = FakeNavigator(DashboardScreen())
    private val presenter = DefaultOptInNotificationPresenter(
        context = context,
        controller = controller,
        remoteConfig = remoteConfig,
        settings = settings,
    )

    // region StateRestorationTester Support
    @get:Rule
    val composeTestRule = createComposeRule()

    private val stateRestorationTester = StateRestorationTester(composeTestRule)

    private fun testPresenterWithStateRestoration(): ReceiveTurbine<UiState> {
        val presenterState = Turbine<UiState>()

        stateRestorationTester.setContent {
            val state = presenter.present(navigator)
            SideEffect { presenterState.add(state) }
        }
        composeTestRule.waitForIdle()

        return presenterState
    }
    // endregion StateRestorationTester Support

    @AfterTest
    fun cleanup() {
        AndroidUiDispatcherUtil.runScheduledDispatches()
        navigator.assertGoToIsEmpty()
        navigator.assertPopIsEmpty()
    }

    // region State.showPrompt
    @Test
    fun `State - showPrompt - shown when eligible`() = runTest {
        presenterTestOf({ presenter.present(navigator) }) {
            assertTrue(awaitItem().showPrompt)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `State - showPrompt - not shown when disabled by remote config`() = runTest {
        isFeatureEnabled = false

        presenterTestOf({ presenter.present(navigator) }) {
            assertFalse(awaitItem().showPrompt)
        }
    }

    @Test
    fun `State - showPrompt - not shown when onboarding has not been completed`() = runTest {
        isOnboardingDiscovered = false

        presenterTestOf({ presenter.present(navigator) }) {
            assertFalse(awaitItem().showPrompt)
        }
    }

    @Test
    fun `State - showPrompt - not shown when permission is already approved`() = runTest {
        controller.permissionStatus.value = PermissionStatus.APPROVED

        presenterTestOf({ presenter.present(navigator) }) {
            assertFalse(awaitItem().showPrompt)
        }
    }

    @Test
    fun `State - showPrompt - not shown when prompt limit is exceeded`() = runTest {
        promptCount = 6

        presenterTestOf({ presenter.present(navigator) }) {
            assertFalse(awaitItem().showPrompt)
        }
    }

    @Test
    fun `State - showPrompt - not shown when prompted within the time interval`() = runTest {
        lastPrompted = LocalDate.now().minusDays(10)

        presenterTestOf({ presenter.present(navigator) }) {
            assertFalse(awaitItem().showPrompt)
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S_V2])
    fun `State - showPrompt - not shown before Android 13`() = runTest {
        presenterTestOf({ presenter.present(navigator) }) {
            assertFalse(awaitItem().showPrompt)
        }
    }

    @Test
    fun `State - showPrompt - not shown again after dismissal and state save & restore`() {
        testPresenterWithStateRestoration().run {
            val state = expectMostRecentItem()
            assertTrue(state.showPrompt)
            state.eventSink(UiEvent.Dismiss)
            composeTestRule.waitForIdle()
            assertFalse(expectMostRecentItem().showPrompt)

            stateRestorationTester.emulateSavedInstanceStateRestore()
            composeTestRule.waitForIdle()
            assertFalse(expectMostRecentItem().showPrompt)
        }
    }
    // endregion State.showPrompt

    // region State.isHardDenied
    @Test
    fun `State - isHardDenied`() = runTest {
        controller.permissionStatus.value = PermissionStatus.HARD_DENIED

        presenterTestOf({ presenter.present(navigator) }) {
            assertTrue(awaitItem().isHardDenied)

            controller.permissionStatus.value = PermissionStatus.SOFT_DENIED
            awaitItemMatching { !it.isHardDenied }
        }
    }
    // endregion State.isHardDenied

    // region UiEvent.AllowNotifications
    @Test
    fun `UiEvent - AllowNotifications - undetermined requests permission`() = runTest {
        presenterTestOf({ presenter.present(navigator) }) {
            awaitItem().eventSink(UiEvent.AllowNotifications)
            awaitItemMatching { !it.showPrompt }
        }

        assertEquals(1, controller.permissionRequests)
        verify { settings.setFeatureDiscovered(FEATURE_OPT_IN_NOTIFICATION) }
    }

    @Test
    fun `UiEvent - AllowNotifications - soft denied requests permission`() = runTest {
        controller.permissionStatus.value = PermissionStatus.SOFT_DENIED

        presenterTestOf({ presenter.present(navigator) }) {
            awaitItem().eventSink(UiEvent.AllowNotifications)
            awaitItemMatching { !it.showPrompt }
        }

        assertEquals(1, controller.permissionRequests)
        verify { settings.setFeatureDiscovered(FEATURE_OPT_IN_NOTIFICATION) }
    }

    @Test
    fun `UiEvent - AllowNotifications - hard denied opens app settings`() = runTest {
        controller.permissionStatus.value = PermissionStatus.HARD_DENIED

        presenterTestOf({ presenter.present(navigator) }) {
            awaitItem().eventSink(UiEvent.AllowNotifications)
            awaitItemMatching { !it.showPrompt }

            val expectedIntent = Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
            assertTrue(assertIs<IntentScreen>(navigator.awaitNextScreen()).intent equalsIntent expectedIntent)
        }

        assertEquals(0, controller.permissionRequests)
        verify(exactly = 0) { settings.setFeatureDiscovered(any()) }
    }

    @Test
    fun `UiEvent - AllowNotifications - system denial does not show the prompt again`() = runTest {
        presenterTestOf({ presenter.present(navigator) }) {
            awaitItem().eventSink(UiEvent.AllowNotifications)
            awaitItemMatching { !it.showPrompt }

            controller.permissionStatus.value = PermissionStatus.HARD_DENIED
            val state = awaitItemMatching { it.isHardDenied }
            assertFalse(state.showPrompt)
        }
    }
    // endregion UiEvent.AllowNotifications

    // region UiEvent.Dismiss
    @Test
    fun `UiEvent - Dismiss`() = runTest {
        presenterTestOf({ presenter.present(navigator) }) {
            awaitItem().eventSink(UiEvent.Dismiss)
            awaitItemMatching { !it.showPrompt }
        }

        assertEquals(0, controller.permissionRequests)
        verify(exactly = 0) { settings.setFeatureDiscovered(any()) }
    }
    // endregion UiEvent.Dismiss

    // region SideEffect - recordOptInNotificationPrompt
    @Test
    fun `SideEffect - recordOptInNotificationPrompt - recorded once when prompt is shown`() = runTest {
        presenterTestOf({ presenter.present(navigator) }) {
            awaitItem().eventSink(UiEvent.Dismiss)
            awaitItemMatching { !it.showPrompt }
        }

        verify(exactly = 1) { settings.recordOptInNotificationPrompt() }
    }

    @Test
    fun `SideEffect - recordOptInNotificationPrompt - not recorded when prompt is not shown`() = runTest {
        isFeatureEnabled = false

        presenterTestOf({ presenter.present(navigator) }) {
            assertFalse(awaitItem().showPrompt)
        }

        verify(exactly = 0) { settings.recordOptInNotificationPrompt() }
    }

    @Test
    fun `SideEffect - recordOptInNotificationPrompt - not recorded again after state save & restore`() {
        testPresenterWithStateRestoration().run {
            assertTrue(expectMostRecentItem().showPrompt)

            stateRestorationTester.emulateSavedInstanceStateRestore()
            composeTestRule.waitForIdle()
            assertTrue(expectMostRecentItem().showPrompt)
        }

        verify(exactly = 1) { settings.recordOptInNotificationPrompt() }
    }
    // endregion SideEffect - recordOptInNotificationPrompt
}
