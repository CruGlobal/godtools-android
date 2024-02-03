package org.cru.godtools.ui.tools

import android.app.Application
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolCardActionsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<ToolCard.Event>()

    @Test
    fun `Button - Open Tool`() {
        val state = ToolCard.State(eventSink = events)
        composeTestRule.setContent { ToolCardActions(state) }

        composeTestRule.onNodeWithText("Open", substring = true, ignoreCase = true).performClick()
        events.assertEvent(ToolCard.Event.OpenTool)
    }

    @Test
    fun `Button - Tool Details`() {
        val state = ToolCard.State(eventSink = events)
        composeTestRule.setContent { ToolCardActions(state) }

        composeTestRule.onNodeWithText("Details", substring = true, ignoreCase = true).performClick()
        events.assertEvent(ToolCard.Event.OpenToolDetails)
    }

    @Test
    fun `Recompose - eventSink updates`() = runTest {
        val stateFlow = MutableStateFlow(ToolCard.State())
        composeTestRule.setContent { ToolCardActions(stateFlow.collectAsState().value) }

        composeTestRule.onNodeWithText("Open", substring = true, ignoreCase = true).performClick()
        composeTestRule.onNodeWithText("Details", substring = true, ignoreCase = true).performClick()
        events.assertNoEvents()

        stateFlow.value = ToolCard.State(eventSink = events)
        composeTestRule.onNodeWithText("Open", substring = true, ignoreCase = true).performClick()
        composeTestRule.onNodeWithText("Details", substring = true, ignoreCase = true).performClick()
        events.assertEvents(
            ToolCard.Event.OpenTool,
            ToolCard.Event.OpenToolDetails
        )
    }
}
