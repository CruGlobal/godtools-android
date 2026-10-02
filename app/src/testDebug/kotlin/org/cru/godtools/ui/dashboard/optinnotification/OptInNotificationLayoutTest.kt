package org.cru.godtools.ui.dashboard.optinnotification

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.test.TestEventSink
import kotlin.test.Test
import org.cru.godtools.R
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationPresenter.UiEvent
import org.cru.godtools.ui.dashboard.optinnotification.OptInNotificationPresenter.UiState
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class OptInNotificationLayoutTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val events = TestEventSink<UiEvent>()

    // region Overlay
    @Test
    fun `Overlay - not shown when showPrompt is false`() = runComposeUiTest {
        setContent { ContentWithOverlays { OptInNotificationLayout(UiState(showPrompt = false, eventSink = events)) } }

        onNodeWithText(context.getString(R.string.opt_in_notification_title)).assertDoesNotExist()
    }

    @Test
    fun `Overlay - shown when showPrompt is true`() = runComposeUiTest {
        setContent { ContentWithOverlays { OptInNotificationLayout(UiState(showPrompt = true, eventSink = events)) } }

        onNodeWithText(context.getString(R.string.opt_in_notification_title)).assertExists()
        onNodeWithText(context.getString(R.string.opt_in_notification_allow_notifications)).assertExists()
        onNodeWithText(context.getString(R.string.opt_in_notification_notification_settings)).assertDoesNotExist()
    }

    @Test
    fun `Overlay - hard denied shows notification settings action`() = runComposeUiTest {
        setContent {
            ContentWithOverlays {
                OptInNotificationLayout(UiState(showPrompt = true, isHardDenied = true, eventSink = events))
            }
        }

        onNodeWithText(context.getString(R.string.opt_in_notification_notification_settings)).assertExists()
        onNodeWithText(context.getString(R.string.opt_in_notification_allow_notifications)).assertDoesNotExist()
    }

    @Test
    fun `Overlay - click Allow Notifications fires AllowNotifications`() = runComposeUiTest {
        setContent { ContentWithOverlays { OptInNotificationLayout(UiState(showPrompt = true, eventSink = events)) } }

        onNodeWithText(context.getString(R.string.opt_in_notification_allow_notifications)).performClick()
        waitForIdle()
        events.assertEvent(UiEvent.AllowNotifications)
    }

    @Test
    fun `Overlay - click Maybe Later fires Dismiss`() = runComposeUiTest {
        setContent { ContentWithOverlays { OptInNotificationLayout(UiState(showPrompt = true, eventSink = events)) } }

        onNodeWithText(context.getString(R.string.opt_in_notification_maybe_later)).performClick()
        waitForIdle()
        events.assertEvent(UiEvent.Dismiss)
    }
    // endregion Overlay
}
