package org.cru.godtools.ui.tools

import android.app.Application
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import org.cru.godtools.downloadmanager.DownloadProgress
import org.cru.godtools.model.randomTool
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class SquareToolCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<ToolCard.Event>()

    @Test
    fun `SquareToolCard()`() {
        val tool = randomTool(name = UUID.randomUUID().toString())

        composeTestRule.setContent { SquareToolCard(ToolCard.State(tool)) }

        composeTestRule.onNodeWithText(tool.name!!).assertExists()
        composeTestRule.onNodeWithTag(TEST_TAG_FAVORITE_ACTION).assertExists()
    }

    @Test
    fun `SquareToolCard() - Event - Click`() {
        composeTestRule.setContent { SquareToolCard(ToolCard.State(eventSink = events)) }

        composeTestRule.onRoot().performClick()
        events.assertEvent(ToolCard.Event.Click)
    }

    // region SquareToolCard - Category
    @Test
    fun `SquareToolCard(showCategory=true)`() {
        composeTestRule.setContent {
            SquareToolCard(
                state = ToolCard.State(randomTool(category = "gospel")),
                showCategory = true
            )
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TOOL_CATEGORY).assertDoesNotExist()
    }

    @Test
    fun `SquareToolCard(showCategory=false)`() {
        composeTestRule.setContent {
            SquareToolCard(
                state = ToolCard.State(randomTool(category = "gospel")),
                showCategory = false
            )
        }

        composeTestRule.onNodeWithTag(TEST_TAG_TOOL_CATEGORY).assertDoesNotExist()
    }
    // endregion SquareToolCard - Category

    // region SquareToolCard - Download Progress
    @Test
    fun `SquareToolCard() - Download Progress - Hidden when not downloading`() {
        composeTestRule.setContent {
            SquareToolCard(state = ToolCard.State(tool = randomTool(), downloadProgress = null))
        }

        composeTestRule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertDoesNotExist()
    }

    @Test
    fun `SquareToolCard() - Download Progress - Visible when downloading`() {
        composeTestRule.setContent {
            SquareToolCard(state = ToolCard.State(tool = randomTool(), downloadProgress = DownloadProgress(1, 4)))
        }

        val progressRangeInfo = composeTestRule
            .onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .assertExists()
            .fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(0.25f, progressRangeInfo.current)
    }
    // endregion SquareToolCard - Download Progress
}
