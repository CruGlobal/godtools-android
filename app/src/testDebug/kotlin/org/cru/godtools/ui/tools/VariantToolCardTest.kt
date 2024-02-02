package org.cru.godtools.ui.tools

import android.app.Application
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher.Companion.expectValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import java.util.UUID
import kotlin.test.Test
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class VariantToolCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<ToolCard.Event>()

    @Test
    fun `VariantToolCard()`() {
        val tool = randomTool(name = UUID.randomUUID().toString())
        val translation = randomTranslation(tool.code, tagline = UUID.randomUUID().toString())

        composeTestRule.setContent { VariantToolCard(ToolCard.State(tool, translation = translation)) }

        composeTestRule.onNodeWithText(translation.name!!).assertExists()
        composeTestRule.onNodeWithText(translation.tagline!!).assertExists()
    }

    @Test
    fun `VariantToolCard() - Event - Click`() {
        composeTestRule.setContent { VariantToolCard(ToolCard.State(eventSink = events)) }

        composeTestRule.onRoot().performClick()
        events.assertEvent(ToolCard.Event.Click)
    }

    // region VariantToolCard - isSelected
    @Test
    fun `VariantToolCard(isSelected=true)`() {
        composeTestRule.setContent {
            VariantToolCard(
                state = ToolCard.State(randomTool(), eventSink = events),
                isSelected = true,
            )
        }

        composeTestRule.onNode(expectValue(SemanticsProperties.Role, Role.RadioButton))
            .assertExists()
            .assertIsSelected()
            .performClick()
        events.assertEvent(ToolCard.Event.Click)
    }

    @Test
    fun `VariantToolCard(isSelected=false)`() {
        composeTestRule.setContent {
            VariantToolCard(
                state = ToolCard.State(randomTool(), eventSink = events),
                isSelected = false,
            )
        }

        composeTestRule.onNode(expectValue(SemanticsProperties.Role, Role.RadioButton))
            .assertExists()
            .assertIsNotSelected()
            .performClick()
        events.assertEvent(ToolCard.Event.Click)
    }
    // endregion VariantToolCard - isSelected
}
