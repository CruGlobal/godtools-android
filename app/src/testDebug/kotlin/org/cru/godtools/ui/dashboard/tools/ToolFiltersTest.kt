package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import java.util.Locale
import kotlin.test.Test
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.dashboard.tools.ToolsScreen.Filters.Filter
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolFiltersTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<ToolsScreen.FiltersEvent>()

    // region CategoriesFilter
    @Test
    fun `CategoryFilter() - Shows selected category`() {
        composeTestRule.setContent {
            CategoryFilter(
                ToolsScreen.Filters(
                    selectedCategory = Tool.CATEGORY_GOSPEL,
                    eventSink = events,
                ),
            )
        }

        composeTestRule.onNodeWithText("Gospel", substring = true, ignoreCase = true).assertExists()
        events.assertNoEvents()
    }

    @Test
    fun `CategoryFilter() - Shows Any Category when no category is specified`() {
        composeTestRule.setContent {
            CategoryFilter(
                ToolsScreen.Filters(
                    selectedCategory = null,
                    eventSink = events,
                ),
            )
        }

        composeTestRule.onNodeWithText("Any category", substring = true, ignoreCase = true).assertExists()
        events.assertNoEvents()
    }

    @Test
    fun `CategoryFilter() - Dropdown Menu - Show when button is clicked`() {
        composeTestRule.setContent {
            CategoryFilter(ToolsScreen.Filters(eventSink = events))
        }

        // dropdown menu not shown
        composeTestRule.onNodeWithTag(TEST_TAG_FILTER_DROPDOWN).assertDoesNotExist()

        // click button to show dropdown
        composeTestRule.onNode(hasClickAction()).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_FILTER_DROPDOWN).assertExists()
        events.assertNoEvents()
    }

    @Test
    fun `CategoryFilter() - Dropdown Menu - Show categories`() {
        composeTestRule.setContent {
            CategoryFilter(
                filters = ToolsScreen.Filters(
                    categories = listOf(
                        Filter(Tool.CATEGORY_GOSPEL, 1),
                        Filter(Tool.CATEGORY_ARTICLES, 1)
                    ),
                    eventSink = events,
                ),
            )
        }
        composeTestRule.onNode(hasClickAction()).performClick()

        composeTestRule.onNodeWithText("Growth", substring = true, ignoreCase = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("Articles", substring = true, ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("Gospel", substring = true, ignoreCase = true).assertExists()
        events.assertNoEvents()
    }

    @Test
    fun `CategoryFilter() - Dropdown Menu - Select 'Any category' option`() {
        composeTestRule.setContent {
            CategoryFilter(
                filters = ToolsScreen.Filters(
                    selectedCategory = Tool.CATEGORY_GOSPEL,
                    categories = listOf(
                        Filter(Tool.CATEGORY_GOSPEL, 1),
                        Filter(Tool.CATEGORY_ARTICLES, 1)
                    ),
                    eventSink = events,
                ),
            )
        }
        composeTestRule.onNode(hasClickAction()).performClick()

        composeTestRule.onNodeWithText("Any category", substring = true, ignoreCase = true).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_FILTER_DROPDOWN).assertDoesNotExist()
        events.assertEvent(ToolsScreen.FiltersEvent.SelectCategory(null))
    }

    @Test
    fun `CategoryFilter() - Dropdown Menu - Select a category`() {
        composeTestRule.setContent {
            CategoryFilter(
                filters = ToolsScreen.Filters(
                    categories = listOf(
                        Filter(Tool.CATEGORY_GOSPEL, 1),
                        Filter(Tool.CATEGORY_ARTICLES, 1)
                    ),
                    eventSink = events,
                ),
            )
        }
        composeTestRule.onNode(hasClickAction()).performClick()

        composeTestRule.onNodeWithText("Gospel", substring = true, ignoreCase = true).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_FILTER_DROPDOWN).assertDoesNotExist()
        events.assertEvent(ToolsScreen.FiltersEvent.SelectCategory(Tool.CATEGORY_GOSPEL))
    }
    // endregion CategoryFilter

    // region LanguageFilter
    @Test
    fun `LanguageFilter() - Shows selectedLanguage`() {
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
    fun `LanguageFilter() - Shows Any Language when no language is specified`() {
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
    fun `LanguageFilter() - Button toggles menu`() {
        composeTestRule.setContent {
            LanguageFilter(
                ToolsScreen.Filters(
                    selectedLanguage = Language(Locale.ENGLISH),
                    eventSink = events,
                )
            )
        }

        // click button to show dropdown
        composeTestRule.onNodeWithText("English").assertHasClickAction().performClick()
        events.assertEvent(ToolsScreen.FiltersEvent.ToggleLanguagesMenu)
    }

    @Test
    fun `LanguageFilter() - Dropdown Menu - Show languages`() {
        composeTestRule.setContent {
            LanguageFilter(
                filters = ToolsScreen.Filters(
                    showLanguagesMenu = true,
                    languages = listOf(
                        Filter(Language(Locale.FRENCH), 1),
                        Filter(Language(Locale.GERMAN), 1),
                    ),
                    eventSink = events,
                ),
            )
        }

        composeTestRule.onNodeWithText("English", substring = true, ignoreCase = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("French", substring = true, ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("German", substring = true, ignoreCase = true).assertExists()
    }

    @Test
    fun `LanguageFilter() - Dropdown Menu - Select 'Any language' option`() {
        composeTestRule.setContent {
            LanguageFilter(
                filters = ToolsScreen.Filters(
                    selectedLanguage = Language(Locale.FRENCH),
                    showLanguagesMenu = true,
                    languages = listOf(
                        Filter(Language(Locale.FRENCH), 1),
                        Filter(Language(Locale.GERMAN), 1),
                    ),
                    eventSink = events,
                ),
            )
        }

        composeTestRule.onNodeWithText("Any language", substring = true, ignoreCase = true).performClick()
        events.assertEvent(ToolsScreen.FiltersEvent.SelectLanguage(null))
    }

    @Test
    fun `LanguageFilter() - Dropdown Menu - Select a language`() {
        composeTestRule.setContent {
            LanguageFilter(
                filters = ToolsScreen.Filters(
                    showLanguagesMenu = true,
                    languages = listOf(
                        Filter(Language(Locale.FRENCH), 1),
                        Filter(Language(Locale.GERMAN), 1),
                    ),
                    eventSink = events,
                ),
            )
        }

        composeTestRule.onNodeWithText("French", substring = true, ignoreCase = true).performClick()
        events.assertEvents(ToolsScreen.FiltersEvent.SelectLanguage(Locale.FRENCH))
    }
    // endregion LanguageFilter
}
