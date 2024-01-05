package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import java.util.Locale
import org.cru.godtools.model.Language
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolFiltersTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<ToolsScreen.FiltersEvent>()

    // region: LanguagesFilter
    @Test
    fun `LanguagesFilter() - Shows selectedLanguage`() {
        composeTestRule.setContent {
            LanguageFilter(
                ToolsScreen.Filters(
                    selectedLanguage = Language(Locale.ENGLISH),
                    eventSink = events,
                ),
            )
        }

        composeTestRule.onNodeWithText("English", substring = true, ignoreCase = true).assertExists()
        events.assertNoEvents()
    }

    @Test
    fun `LanguagesFilter() - Shows Any Language when no language is specified`() {
        composeTestRule.setContent {
            LanguageFilter(
                ToolsScreen.Filters(
                    selectedLanguage = null,
                    eventSink = events,
                ),
            )
        }

        composeTestRule.onNodeWithText("Any language", substring = true, ignoreCase = true).assertExists()
        events.assertNoEvents()
    }

    @Test
    fun `LanguagesFilter() - Dropdown Menu - Show when button is clicked`() {
        composeTestRule.setContent {
            LanguageFilter(ToolsScreen.Filters(eventSink = events))
        }

        // dropdown menu not shown
        composeTestRule.onNodeWithTag(TEST_TAG_FILTER_DROPDOWN).assertDoesNotExist()

        // click button to show dropdown
        composeTestRule.onNode(hasClickAction()).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_FILTER_DROPDOWN).assertExists()
        events.assertEvent(ToolsScreen.FiltersEvent.UpdateLanguageQuery(""))
    }

    @Test
    fun `LanguagesFilter() - Dropdown Menu - Show languages`() {
        composeTestRule.setContent {
            LanguageFilter(
                filters = ToolsScreen.Filters(
                    languages = listOf(
                        Language(Locale.FRENCH),
                        Language(Locale.GERMAN),
                    ),
                    eventSink = events,
                ),
            )
        }
        composeTestRule.onNode(hasClickAction()).performClick()

        composeTestRule.onNodeWithText("English", substring = true, ignoreCase = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("French", substring = true, ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("German", substring = true, ignoreCase = true).assertExists()
        events.assertEvent(ToolsScreen.FiltersEvent.UpdateLanguageQuery(""))
    }

    @Test
    fun `LanguagesFilter() - Dropdown Menu - Select "Any language" option`() {
        composeTestRule.setContent {
            LanguageFilter(
                filters = ToolsScreen.Filters(
                    selectedLanguage = Language(Locale.FRENCH),
                    languages = listOf(
                        Language(Locale.FRENCH),
                        Language(Locale.GERMAN)
                    ),
                    eventSink = events,
                ),
            )
        }
        composeTestRule.onNode(hasClickAction()).performClick()

        composeTestRule.onNodeWithText("Any language", substring = true, ignoreCase = true).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_FILTER_DROPDOWN).assertDoesNotExist()
        events.assertEvents(
            ToolsScreen.FiltersEvent.UpdateLanguageQuery(""),
            ToolsScreen.FiltersEvent.SelectLanguage(null)
        )
    }

    @Test
    fun `LanguagesFilter() - Dropdown Menu - Select a language`() {
        composeTestRule.setContent {
            LanguageFilter(
                filters = ToolsScreen.Filters(
                    languages = listOf(
                        Language(Locale.FRENCH),
                        Language(Locale.GERMAN)
                    ),
                    eventSink = events,
                ),
            )
        }
        composeTestRule.onNode(hasClickAction()).performClick()

        composeTestRule.onNodeWithText("French", substring = true, ignoreCase = true).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_FILTER_DROPDOWN).assertDoesNotExist()
        events.assertEvents(
            ToolsScreen.FiltersEvent.UpdateLanguageQuery(""),
            ToolsScreen.FiltersEvent.SelectLanguage(Locale.FRENCH)
        )
    }
    // endregion: LanguagesFilter
}
