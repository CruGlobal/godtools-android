package org.cru.godtools.ui.tools

import android.app.Application
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher.Companion.expectValue
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onSiblings
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToString
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import java.util.Locale
import java.util.UUID
import kotlin.test.Test
import org.cru.godtools.model.Language
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

        composeTestRule.setContent { VariantToolCard(ToolCard.State(tool = tool, translation = translation)) }

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
                state = ToolCard.State(tool = randomTool(), eventSink = events),
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
                state = ToolCard.State(tool = randomTool(), eventSink = events),
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

    // region VariantToolCard - App Language
    @Test
    fun `VariantToolCard - App Language - Available`() {
        composeTestRule.setContent {
            VariantToolCard(
                state = ToolCard.State(
                    tool = randomTool(),
                    appLanguage = Language(Locale.ENGLISH),
                    appLanguageAvailable = true,
                ),
            )
        }

        composeTestRule.onNodeWithText("English", useUnmergedTree = true)
            .onSiblings()
            .filterToOne(expectValue(SemanticsProperties.Role, Role.Image))
            .assertContentDescriptionEquals("Available")
    }

    @Test
    fun `VariantToolCard - App Language - Not Available`() {
        composeTestRule.setContent {
            VariantToolCard(
                state = ToolCard.State(
                    tool = randomTool(),
                    appLanguage = Language(Locale.ENGLISH),
                    appLanguageAvailable = false,
                ),
            )
        }

        composeTestRule.onNodeWithText("Not available in English").assertExists()
    }
    // endregion VariantToolCard - App Language

    // region VariantToolCard - Second Language
    @Test
    fun `VariantToolCard - Second Language - Available`() {
        composeTestRule.setContent {
            VariantToolCard(
                state = ToolCard.State(
                    tool = randomTool(),
                    appLanguage = Language(Locale.ENGLISH),
                    appLanguageAvailable = false,
                    secondLanguage = Language(Locale.FRENCH),
                    secondLanguageAvailable = true,
                ),
            )
        }

        composeTestRule.onNodeWithText("French", useUnmergedTree = true)
            .onSiblings()
            .filterToOne(expectValue(SemanticsProperties.Role, Role.Image))
            .assertContentDescriptionEquals("Available")
            .printToString().let { println(it) }
    }

    @Test
    fun `VariantToolCard - Second Language - Not Available`() {
        composeTestRule.setContent {
            VariantToolCard(
                state = ToolCard.State(
                    tool = randomTool(),
                    appLanguage = Language(Locale.ENGLISH),
                    appLanguageAvailable = true,
                    secondLanguage = Language(Locale.FRENCH),
                    secondLanguageAvailable = false,
                ),
            )
        }

        composeTestRule.onNodeWithText("Not available in French").assertExists()
    }
    // endregion VariantToolCard - Second Language
}
