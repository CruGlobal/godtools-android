package org.cru.godtools.ui.tools

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import kotlin.test.Test
import org.cru.godtools.R
import org.cru.godtools.model.randomTool
import org.cru.godtools.ui.tools.ToolCardPresenter.UiEvent
import org.cru.godtools.ui.tools.ToolCardPresenter.UiState
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class FavoriteActionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val events = TestEventSink<UiEvent>()

    // region FavoriteAction()
    @Test
    fun `FavoriteAction() - add to favorites`() {
        val state = UiState(
            tool = randomTool(isFavorite = false),
            eventSink = events,
        )
        composeTestRule.setContent { FavoriteAction(state) }

        composeTestRule.onRoot().performClick()
        events.assertEvent(UiEvent.PinTool)
    }

    @Test
    fun `FavoriteAction() - add to favorites - accessibility`() {
        val state = UiState(
            tool = randomTool(isFavorite = false),
            eventSink = events,
        )
        composeTestRule.setContent { FavoriteAction(state) }

        val label = context.getString(R.string.action_tools_add_favorite)
        composeTestRule.onAllNodesWithContentDescription(label, useUnmergedTree = true).assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.action_tools_remove_favorite))
            .assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(label).assertHasClickAction().performClick()
        events.assertEvent(UiEvent.PinTool)
    }

    @Test
    fun `FavoriteAction() - remove from favorites`() {
        val state = UiState(
            tool = randomTool(isFavorite = true),
            eventSink = events,
        )
        composeTestRule.setContent { FavoriteAction(state, confirmRemoval = false) }

        composeTestRule.onRoot().performClick()
        composeTestRule.onNode(isDialog()).assertDoesNotExist()
        events.assertEvent(UiEvent.UnpinTool)
    }

    @Test
    fun `FavoriteAction() - remove from favorites - accessibility`() {
        val state = UiState(
            tool = randomTool(isFavorite = true),
            eventSink = events,
        )
        composeTestRule.setContent { FavoriteAction(state, confirmRemoval = false) }

        val label = context.getString(R.string.action_tools_remove_favorite)
        composeTestRule.onAllNodesWithContentDescription(label, useUnmergedTree = true).assertCountEquals(1)
        composeTestRule.onNodeWithContentDescription(context.getString(R.string.action_tools_add_favorite))
            .assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(label).assertHasClickAction().performClick()
        events.assertEvent(UiEvent.UnpinTool)
    }

    @Test
    fun `FavoriteAction() - remove from favorites - confirmRemoval - confirm`() {
        val state = UiState(
            tool = randomTool(isFavorite = true),
            eventSink = events,
        )
        composeTestRule.setContent { FavoriteAction(state, confirmRemoval = true) }

        composeTestRule.onRoot().performClick()
        composeTestRule.onNode(isDialog()).assertIsDisplayed()
        events.assertNoEvents()

        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasClickAction() and hasText("Remove")).performClick()
        composeTestRule.onNode(isDialog()).assertDoesNotExist()
        events.assertEvent(UiEvent.UnpinTool)
    }

    @Test
    fun `FavoriteAction() - remove from favorites - confirmRemoval - cancel`() {
        val state = UiState(
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
