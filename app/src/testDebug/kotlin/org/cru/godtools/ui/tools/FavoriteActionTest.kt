package org.cru.godtools.ui.tools

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import kotlin.test.Test
import org.cru.godtools.model.randomTool
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class FavoriteActionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<ToolCard.Event>()

    // region FavoriteAction()
    @Test
    fun `FavoriteAction() - add to favorites`() {
        val state = ToolCard.State(
            tool = randomTool(isFavorite = false),
            eventSink = events,
        )
        composeTestRule.setContent { FavoriteAction(state) }

        composeTestRule.onRoot().performClick()
        events.assertEvent(ToolCard.Event.PinTool)
    }

    @Test
    fun `FavoriteAction() - remove from favorites`() {
        val state = ToolCard.State(
            tool = randomTool(isFavorite = true),
            eventSink = events,
        )
        composeTestRule.setContent { FavoriteAction(state, confirmRemoval = false) }

        composeTestRule.onRoot().performClick()
        composeTestRule.onNode(isDialog()).assertDoesNotExist()
        events.assertEvent(ToolCard.Event.UnpinTool)
    }

    @Test
    fun `FavoriteAction() - remove from favorites - confirmRemoval - confirm`() {
        val state = ToolCard.State(
            tool = randomTool(isFavorite = true),
            eventSink = events,
        )
        composeTestRule.setContent { FavoriteAction(state, confirmRemoval = true) }

        composeTestRule.onRoot().performClick()
        composeTestRule.onNode(isDialog()).assertIsDisplayed()
        events.assertNoEvents()

        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasClickAction() and hasText("Remove")).performClick()
        composeTestRule.onNode(isDialog()).assertDoesNotExist()
        events.assertEvent(ToolCard.Event.UnpinTool)
    }

    @Test
    fun `FavoriteAction() - remove from favorites - confirmRemoval - cancel`() {
        val state = ToolCard.State(
            tool = randomTool(isFavorite = true),
            eventSink = events,
        )
        composeTestRule.setContent { FavoriteAction(state, confirmRemoval = true) }

        composeTestRule.onRoot().performClick()
        composeTestRule.onNode(isDialog()).assertIsDisplayed()
        events.assertNoEvents()

        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasClickAction() and hasText("Cancel")).performClick()
        composeTestRule.onNode(isDialog()).assertDoesNotExist()
        events.assertNoEvents()
    }
    // endregion FavoriteAction()
}
