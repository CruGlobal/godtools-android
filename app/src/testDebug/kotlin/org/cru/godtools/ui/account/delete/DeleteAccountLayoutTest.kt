package org.cru.godtools.ui.account.delete

import android.app.Application
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import kotlin.test.Ignore
import kotlin.test.Test
import org.cru.godtools.ui.account.delete.DeleteAccountScreen.Event
import org.cru.godtools.ui.account.delete.DeleteAccountScreen.State
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DeleteAccountLayoutTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<Event>()

    // region State Display
    @Test
    fun `State Display - Action - Close Icon`() {
        composeTestRule.run {
            setContent { DeleteAccountLayout(State.Display(events)) }
            onNodeWithTag(TEST_TAG_ICON_CLOSE)
                .assertIsEnabled()
                .performClick()
        }

        events.assertEvent(Event.Close)
    }

    @Test
    fun `State Display - Action - Delete Button`() {
        composeTestRule.run {
            setContent { DeleteAccountLayout(State.Display(events)) }
            onNodeWithTag(TEST_TAG_BUTTON_DELETE)
                .assertIsEnabled()
                .performClick()
        }

        events.assertEvent(Event.DeleteAccount)
    }

    @Test
    fun `State Display - Action - Cancel Button`() {
        composeTestRule.run {
            setContent { DeleteAccountLayout(State.Display(events)) }
            onNodeWithTag(TEST_TAG_BUTTON_CANCEL)
                .assertIsEnabled()
                .performClick()
        }

        events.assertEvent(Event.Close)
    }

    @Test
    fun `State Display - No Error Dialog`() {
        composeTestRule.run {
            setContent { DeleteAccountLayout(State.Display(events)) }
            onNode(isDialog()).assertDoesNotExist()
        }

        events.assertNoEvents()
    }
    // endregion State Display

    // region State Deleting
    @Test
    fun `State Deleting - Action - Close Icon`() {
        composeTestRule.run {
            setContent { DeleteAccountLayout(State.Deleting(events)) }
            onNodeWithTag(TEST_TAG_ICON_CLOSE)
                .assertIsEnabled()
                .performClick()
        }

        events.assertEvent(Event.Close)
    }

    @Test
    fun `State Deleting - Action - Disabled Delete & Cancel Buttons`() {
        composeTestRule.run {
            setContent { DeleteAccountLayout(State.Deleting(events)) }
            onNodeWithTag(TEST_TAG_BUTTON_DELETE).assertIsNotEnabled()
            onNodeWithTag(TEST_TAG_BUTTON_CANCEL).assertIsNotEnabled()
        }

        events.assertNoEvents()
    }
    // endregion State Deleting

    // region State Error
    @Test
    fun `State Error - Action - Close Icon`() {
        composeTestRule.run {
            setContent { DeleteAccountLayout(State.Error(events)) }
            onNodeWithTag(TEST_TAG_ICON_CLOSE)
                .assertIsEnabled()
                .performClick()
        }

        events.assertEvent(Event.Close)
    }

    @Test
    fun `State Error - Action - Disabled Delete & Cancel Buttons`() {
        composeTestRule.run {
            setContent { DeleteAccountLayout(State.Error(events)) }
            onNodeWithTag(TEST_TAG_BUTTON_DELETE).assertIsNotEnabled()
            onNodeWithTag(TEST_TAG_BUTTON_CANCEL).assertIsNotEnabled()
        }

        events.assertNoEvents()
    }

    @Test
    fun `State Error - Error Dialog`() {
        composeTestRule.run {
            setContent { DeleteAccountLayout(State.Error(events)) }
            onNode(isDialog() and hasAnyDescendant(hasTestTag(TEST_TAG_ERROR_DIALOG)))
                .assertExists()
        }

        events.assertNoEvents()
    }

    @Test
    fun `State Error - Error Dialog - Confirm Button`() {
        composeTestRule.run {
            setContent { DeleteAccountLayout(State.Error(events)) }
            onNode(
                hasAnyAncestor(hasTestTag(TEST_TAG_ERROR_DIALOG)) and
                    hasTestTag(TEST_TAG_ERROR_DIALOG_BUTTON_CONFIRM)
            )
                .assertIsEnabled()
                .performClick()
        }

        events.assertEvent(Event.ClearError)
    }

    @Test
    @Ignore("It's not currently possible to dismiss the dialog from a test.")
    fun `State Error - Error Dialog - Dismiss Dialog`() {
        composeTestRule.run {
            setContent { DeleteAccountLayout(State.Error(events)) }
            onNode(isDialog() and hasTestTag(TEST_TAG_ERROR_DIALOG)).assertExists()
            // TODO: Dismiss the dialog
            //       see: https://issuetracker.google.com/issues/229759201
        }

        events.assertEvent(Event.ClearError)
    }
    // endregion State Error
}
