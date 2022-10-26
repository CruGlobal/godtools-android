package org.cru.godtools.ui.tooldetails

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.ccci.gto.android.common.kotlin.coroutines.flow.StateFlowValue
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

    private val toolViewModel: ToolViewModels.ToolViewModel = mockk {
        every { tool } returns toolFlow
        every { firstTranslation } returns firstTranslationFlow
        every { secondTranslation } returns MutableStateFlow(null)
        every { firstManifest } returns manifestFlow

        excludeRecords {
            tool
            firstTranslation
            secondTranslation
            firstManifest
        }
    }

    // region ToolDetailsActions()
    @Test
    fun `ToolDetailsActions() - Tool Training Button - visible when manifest has tips`() {
        composeTestRule.setContent { ToolDetailsActions(toolViewModel) }
        manifestFlow.value = mockk { every { hasTips } returns true }

        composeTestRule.onNodeWithTag("action_tool_training").assertExists()
    }

    @Test
    fun `ToolDetailsActions() - Tool Training Button - gone when manifest does not have tips`() {
        composeTestRule.setContent { ToolDetailsActions(toolViewModel) }
        manifestFlow.value = mockk { every { hasTips } returns false }

        composeTestRule.onNodeWithTag("action_tool_training").assertDoesNotExist()
    }

    @Test
    fun `ToolDetailsActions() - Tool Training Button - gone when manifest does not exist`() {
        composeTestRule.setContent { ToolDetailsActions(toolViewModel) }
        manifestFlow.value = null

        composeTestRule.onNodeWithTag("action_tool_training").assertDoesNotExist()
    }
    // endregion ToolDetailsActions()
}
