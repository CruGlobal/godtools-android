package org.cru.godtools.ui.account.delete

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import kotlin.test.Test
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DeleteAccountLayoutTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<DeleteAccountScreen.Event>()

    @Test
    fun `Action - Close Icon`() {
        composeTestRule.run {
            setContent {
                DeleteAccountLayout(DeleteAccountScreen.State(events))
            }

            onNodeWithTag(TEST_TAG_ICON_CLOSE).performClick()
        }

        events.assertEvent(DeleteAccountScreen.Event.Cancel)
    }

    @Test
    fun `Action - Delete Button`() {
        composeTestRule.run {
            setContent {
                DeleteAccountLayout(DeleteAccountScreen.State(events))
            }

            onNodeWithTag(TEST_TAG_BUTTON_DELETE).performClick()
        }

        events.assertEvent(DeleteAccountScreen.Event.DeleteAccount)
    }

    @Test
    fun `Action - Cancel Button`() {
        composeTestRule.run {
            setContent {
                DeleteAccountLayout(DeleteAccountScreen.State(events))
            }

            onNodeWithTag(TEST_TAG_BUTTON_CANCEL).performClick()
        }

        events.assertEvent(DeleteAccountScreen.Event.Cancel)
    }
}
