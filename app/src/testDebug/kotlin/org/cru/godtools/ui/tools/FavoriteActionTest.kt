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
import io.mockk.Called
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import kotlinx.coroutines.flow.MutableStateFlow
import org.ccci.gto.android.common.kotlin.coroutines.flow.StateFlowValue
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.randomTool
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class FavoriteActionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val toolFlow = MutableStateFlow<Tool?>(null)
    private val firstTranslationFlow = MutableStateFlow(StateFlowValue.Initial<Translation?>(null))

    private val toolViewModel: ToolViewModels.ToolViewModel = mockk {
        every { tool } returns toolFlow
        every { firstTranslation } returns firstTranslationFlow
        every { pinTool() } returns mockk()
        every { unpinTool() } returns mockk()

        excludeRecords {
            tool
            firstTranslation
        }
    }

    // region FavoriteAction()
    @Test
    fun `FavoriteAction() - add to favorites`() {
        composeTestRule.setContent { FavoriteAction(toolViewModel) }
        toolFlow.value = randomTool(isFavorite = false)

        composeTestRule.onRoot().performClick()
        verifyAll { toolViewModel.pinTool() }
    }

    @Test
    fun `FavoriteAction() - remove from favorites`() {
        composeTestRule.setContent { FavoriteAction(toolViewModel, confirmRemoval = false) }
        toolFlow.value = randomTool(isFavorite = true)

        composeTestRule.onRoot().performClick()
        composeTestRule.onNode(isDialog()).assertDoesNotExist()
        verifyAll { toolViewModel.unpinTool() }
    }

    @Test
    fun `FavoriteAction() - remove from favorites - confirmRemoval - confirm`() {
        composeTestRule.setContent { FavoriteAction(toolViewModel, confirmRemoval = true) }
        toolFlow.value = randomTool(isFavorite = true)

        composeTestRule.onRoot().performClick()
        composeTestRule.onNode(isDialog()).assertIsDisplayed()
        verify { toolViewModel wasNot Called }

        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasClickAction() and hasText("Remove")).performClick()
        composeTestRule.onNode(isDialog()).assertDoesNotExist()
        verifyAll { toolViewModel.unpinTool() }
    }

    @Test
    fun `FavoriteAction() - remove from favorites - confirmRemoval - cancel`() {
        composeTestRule.setContent { FavoriteAction(toolViewModel, confirmRemoval = true) }
        toolFlow.value = randomTool(isFavorite = true)

        composeTestRule.onRoot().performClick()
        composeTestRule.onNode(isDialog()).assertIsDisplayed()
        verify { toolViewModel wasNot Called }

        composeTestRule.onNode(hasAnyAncestor(isDialog()) and hasClickAction() and hasText("Cancel")).performClick()
        composeTestRule.onNode(isDialog()).assertDoesNotExist()
        verify { toolViewModel wasNot Called }
    }
    // endregion FavoriteAction()
}
