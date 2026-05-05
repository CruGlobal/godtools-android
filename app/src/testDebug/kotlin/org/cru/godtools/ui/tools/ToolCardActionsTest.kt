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
import org.cru.godtools.ui.tools.ToolCardPresenter.ToolCardEvent
import org.cru.godtools.ui.tools.ToolCardPresenter.UiEvent
import org.cru.godtools.ui.tools.ToolCardPresenter.UiState
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolCardActionsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<UiEvent>()

    @Test
    fun `Button - Open Tool`() {
        val state = UiState(eventSink = events)
        composeTestRule.setContent { ToolCardActions(state) }

        composeTestRule.onNodeWithText("Open", substring = true, ignoreCase = true).performClick()
        events.assertEvent(ToolCardEvent.OpenTool)
    }

    @Test
    fun `Button - Tool Details`() {
        val state = UiState(eventSink = events)
        composeTestRule.setContent { ToolCardActions(state) }

        composeTestRule.onNodeWithText("Details", substring = true, ignoreCase = true).performClick()
        events.assertEvent(ToolCardEvent.OpenToolDetails)
    }

    @Test
    fun `Recompose - eventSink updates`() = runTest {
        val stateFlow = MutableStateFlow(UiState())
        composeTestRule.setContent { ToolCardActions(stateFlow.collectAsState().value) }

        composeTestRule.onNodeWithText("Open", substring = true, ignoreCase = true).performClick()
        composeTestRule.onNodeWithText("Details", substring = true, ignoreCase = true).performClick()
        events.assertNoEvents()

        stateFlow.value = UiState(eventSink = events)
        composeTestRule.onNodeWithText("Open", substring = true, ignoreCase = true).performClick()
        composeTestRule.onNodeWithText("Details", substring = true, ignoreCase = true).performClick()
        events.assertEvents(
            ToolCardEvent.OpenTool,
            ToolCardEvent.OpenToolDetails
        )
    }
}
