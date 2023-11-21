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
import io.mockk.excludeRecords
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.ccci.gto.android.common.kotlin.coroutines.flow.StateFlowValue
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.ui.tools.ToolViewModels
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolDetailsLayoutTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val toolFlow = MutableStateFlow<Tool?>(null)
    private val firstTranslationFlow = MutableStateFlow(StateFlowValue.Initial<Translation?>(null))
    private val manifestFlow = MutableStateFlow<Manifest?>(null)
    private val availableLanguagesFlow = MutableStateFlow(emptyList<Language>())

    private val viewModel: ToolDetailsViewModel = mockk()
    private val toolViewModel: ToolViewModels.ToolViewModel = mockk {
        every { tool } returns toolFlow
        every { firstTranslation } returns firstTranslationFlow
        every { secondTranslation } returns MutableStateFlow(null)
        every { firstManifest } returns manifestFlow
        every { availableLanguages } returns availableLanguagesFlow

        excludeRecords {
            tool
            firstTranslation
            secondTranslation
            firstManifest
            availableLanguages
        }
    }

    // region ToolDetailsActions()
    @Test
    fun `ToolDetailsActions() - Tool Training Button - visible when manifest has tips`() {
        composeTestRule.setContent { ToolDetailsActions(viewModel, toolViewModel) }
        manifestFlow.value = mockk { every { hasTips } returns true }

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_TOOL_TRAINING).assertExists()
    }

    @Test
    fun `ToolDetailsActions() - Tool Training Button - gone when manifest does not have tips`() {
        composeTestRule.setContent { ToolDetailsActions(viewModel, toolViewModel) }
        manifestFlow.value = mockk { every { hasTips } returns false }

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_TOOL_TRAINING).assertDoesNotExist()
    }

    @Test
    fun `ToolDetailsActions() - Tool Training Button - gone when manifest does not exist`() {
        composeTestRule.setContent { ToolDetailsActions(viewModel, toolViewModel) }
        manifestFlow.value = null

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_TOOL_TRAINING).assertDoesNotExist()
    }
    // endregion ToolDetailsActions()

    // region ToolDetailsLanguages()
    @Test
    fun `ToolDetailsLanguages() - No Languages`() {
        composeTestRule.setContent { ToolDetailsLanguages(toolViewModel, true, {}) }
        availableLanguagesFlow.value = emptyList()

        // The entire ToolDetailsLanguages() composable should be gone if there are no languages
        composeTestRule.onRoot().onChildren().assertCountEquals(0)
    }

    @Test
    fun `ToolDetailsLanguages() - Sorted Languages`() {
        composeTestRule.setContent { ToolDetailsLanguages(toolViewModel, true, {}) }
        availableLanguagesFlow.value = listOf(
            Language(Language.INVALID_CODE, name = "Language 2"),
            Language(Language.INVALID_CODE, name = "Language 1"),
        )

        composeTestRule.onNodeWithTag(TEST_TAG_LANGUAGES_AVAILABLE).assertTextEquals("Language 1, Language 2")
    }
    // endregion ToolDetailsLanguages()
}
