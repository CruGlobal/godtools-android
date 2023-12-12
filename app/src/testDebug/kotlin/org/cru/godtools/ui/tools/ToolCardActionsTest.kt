package org.cru.godtools.ui.tools

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolCardActionsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<ToolCard.Event>()

    @Test
    fun `ToolCardActions() - Open Tool`() {
        val state = ToolCard.State(eventSink = events)
        composeTestRule.setContent { ToolCardActions(state) }

        composeTestRule.onNodeWithText("Open", substring = true, ignoreCase = true).performClick()
        events.assertEvent(ToolCard.Event.OpenTool)
    }

    @Test
    fun `ToolCardActions() - Tool Details`() {
        val state = ToolCard.State(eventSink = events)
        composeTestRule.setContent { ToolCardActions(state) }

        composeTestRule.onNodeWithText("Details", substring = true, ignoreCase = true).performClick()
        events.assertEvent(ToolCard.Event.OpenToolDetails)
    }
}
