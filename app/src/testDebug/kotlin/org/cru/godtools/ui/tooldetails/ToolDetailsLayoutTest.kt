package org.cru.godtools.ui.tooldetails

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolDetailsLayoutTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // region ToolDetailsActions()
    @Test
    fun `ToolDetailsActions() - Tool Training Button - visible when manifest has tips`() {
        val state = ToolDetailsScreen.State(manifest = mockk { every { hasTips } returns true })
        composeTestRule.setContent { ToolDetailsActions(state) }

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_TOOL_TRAINING).assertExists()
    }

    @Test
    fun `ToolDetailsActions() - Tool Training Button - gone when manifest does not have tips`() {
        val state = ToolDetailsScreen.State(manifest = mockk { every { hasTips } returns false })
        composeTestRule.setContent { ToolDetailsActions(state) }

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_TOOL_TRAINING).assertDoesNotExist()
    }

    @Test
    fun `ToolDetailsActions() - Tool Training Button - gone when manifest does not exist`() {
        val state = ToolDetailsScreen.State(manifest = null)
        composeTestRule.setContent { ToolDetailsActions(state) }

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_TOOL_TRAINING).assertDoesNotExist()
    }
    // endregion ToolDetailsActions()

    // region ToolDetailsLanguages()
    @Test
    fun `ToolDetailsLanguages() - No Languages`() {
        val state = ToolDetailsScreen.State(availableLanguages = persistentListOf())
        composeTestRule.setContent { ToolDetailsLanguages(state, true, {}) }

        // The entire ToolDetailsLanguages() composable should be gone if there are no languages
        composeTestRule.onRoot().onChildren().assertCountEquals(0)
    }

    @Test
    fun `ToolDetailsLanguages() - Sorted Languages`() {
        val state = ToolDetailsScreen.State(
            availableLanguages = persistentListOf("Language 1", "Language 2")
        )
        composeTestRule.setContent { ToolDetailsLanguages(state, true, {}) }

        composeTestRule.onNodeWithTag(TEST_TAG_LANGUAGES_AVAILABLE).assertTextEquals("Language 1, Language 2")
    }
    // endregion ToolDetailsLanguages()
}
