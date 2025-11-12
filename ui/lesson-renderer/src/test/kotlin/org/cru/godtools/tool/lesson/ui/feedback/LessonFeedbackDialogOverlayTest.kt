package org.cru.godtools.tool.lesson.ui.feedback

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.ccci.gto.android.common.circuit.overlay.TestOverlayNavigator
import org.cru.godtools.tool.lesson.R
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LessonFeedbackDialogOverlayTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val navigator = TestOverlayNavigator<LessonFeedbackDialogOverlay.Result>()

    // region Nodes
    private fun stringRes(@StringRes id: Int) = composeTestRule.activity.getString(id)
    private val onHelpfulYes
        get() = composeTestRule.onNodeWithText(stringRes(R.string.lesson_feedback_question_helpful_yes))
    private val onHelpfulNo
        get() = composeTestRule.onNodeWithText(stringRes(R.string.lesson_feedback_question_helpful_no))
    private val onReadinessSlider
        get() = composeTestRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
    private val onActionSubmit get() = composeTestRule.onNodeWithText(stringRes(R.string.lesson_feedback_action_submit))
    private val onActionSkip get() = composeTestRule.onNodeWithText(stringRes(R.string.lesson_feedback_action_cancel))

    private val SemanticsNode.progressBarRangeInfo get() = config[SemanticsProperties.ProgressBarRangeInfo]
    private fun SemanticsNode.setProgress(value: Float) = config[SemanticsActions.SetProgress].action!!.invoke(value)
    // endregion Nodes

    @Test
    fun `UI - Helpful Selector`() {
        val overlay = LessonFeedbackDialogOverlay(0)
        composeTestRule.setContent { overlay.Content(navigator) }

        // default to neither selected
        onHelpfulYes.assertIsNotSelected()
        onHelpfulNo.assertIsNotSelected()

        // Select "Yes"
        onHelpfulYes.performClick()
        onHelpfulYes.assertIsSelected()
        onHelpfulNo.assertIsNotSelected()

        // Select "No"
        onHelpfulNo.performClick()
        onHelpfulYes.assertIsNotSelected()
        onHelpfulNo.assertIsSelected()

        // Toggle "No" off
        onHelpfulNo.performClick()
        onHelpfulYes.assertIsNotSelected()
        onHelpfulNo.assertIsNotSelected()

        // Toggle "Yes" on & off
        onHelpfulYes.performClick()
        onHelpfulYes.assertIsSelected()
        onHelpfulYes.performClick()
        onHelpfulYes.assertIsNotSelected()
        onHelpfulNo.assertIsNotSelected()
    }

    @Test
    fun `UI - Readiness Slider`() {
        val overlay = LessonFeedbackDialogOverlay(0)
        composeTestRule.setContent { overlay.Content(navigator) }

        with(onReadinessSlider.fetchSemanticsNode().progressBarRangeInfo) {
            assertEquals(1f, current)
            assertEquals(1f..10f, range)
            assertEquals(8, steps)
        }

        onReadinessSlider.fetchSemanticsNode().setProgress(3f)
        assertEquals(3f, onReadinessSlider.fetchSemanticsNode().progressBarRangeInfo.current)
    }

    @Test
    fun `Action - Submit`() {
        val pageReached = Random.nextInt(1, 100)
        val readiness = Random.nextInt(1, 10)
        val overlay = LessonFeedbackDialogOverlay(pageReached)
        composeTestRule.setContent { overlay.Content(navigator) }

        onHelpfulYes.performClick()
        onReadinessSlider.fetchSemanticsNode().setProgress(readiness.toFloat())
        onActionSubmit.performClick()
        assertEquals(
            LessonFeedbackDialogOverlay.Result.Submit(pageReached, true, readiness),
            navigator.result
        )
    }

    @Test
    fun `Action - Submit - Helpful - not answered`() {
        val overlay = LessonFeedbackDialogOverlay(1)
        composeTestRule.setContent { overlay.Content(navigator) }

        onActionSubmit.performClick()
        assertNull(assertIs<LessonFeedbackDialogOverlay.Result.Submit>(navigator.result).helpful)
    }

    @Test
    fun `Action - Submit - Helpful - yes`() {
        val overlay = LessonFeedbackDialogOverlay(1)
        composeTestRule.setContent { overlay.Content(navigator) }

        onHelpfulYes.performClick()
        onActionSubmit.performClick()
        assertTrue(assertIs<LessonFeedbackDialogOverlay.Result.Submit>(navigator.result).helpful!!)
    }

    @Test
    fun `Action - Submit - Helpful - no`() {
        val overlay = LessonFeedbackDialogOverlay(1)
        composeTestRule.setContent { overlay.Content(navigator) }

        onHelpfulNo.performClick()
        onActionSubmit.performClick()
        assertFalse(assertIs<LessonFeedbackDialogOverlay.Result.Submit>(navigator.result).helpful!!)
    }

    @Test
    fun `Action - Cancel`() {
        val overlay = LessonFeedbackDialogOverlay(0)
        composeTestRule.setContent { overlay.Content(navigator) }

        onActionSkip.performClick()
        assertEquals(LessonFeedbackDialogOverlay.Result.Skip, navigator.result)
    }
}
