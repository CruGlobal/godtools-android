package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.compose.runtime.mutableStateOf
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
import kotlin.test.assertTrue
import kotlinx.collections.immutable.persistentListOf
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.dashboard.filters.FilterMenu.Event
import org.cru.godtools.ui.dashboard.filters.FilterMenu.UiState
import org.cru.godtools.ui.dashboard.filters.FilterMenu.UiState.Item
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolFiltersTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<Event<*>>()

    // region CategoriesFilter
    @Test
    fun `CategoryFilter() - Shows selected category`() {
        composeTestRule.setContent {
            CategoryFilter(
                UiState(
                    selectedItem = Tool.CATEGORY_GOSPEL,
                    eventSink = events,
                )
            )
        }

        composeTestRule.onNodeWithText("Gospel", substring = true, ignoreCase = true).assertExists()
        events.assertNoEvents()
    }

    @Test
    fun `CategoryFilter() - Shows Any Category when no category is specified`() {
        composeTestRule.setContent {
            CategoryFilter(
                UiState(
                    selectedItem = null,
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
            CategoryFilter(UiState(eventSink = events))
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
                UiState(
                    items = persistentListOf(
                        Item(Tool.CATEGORY_GOSPEL, 1),
                        Item(Tool.CATEGORY_ARTICLES, 1)
                    ),
                    eventSink = events
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
                UiState(
                    items = persistentListOf(
                        Item(Tool.CATEGORY_GOSPEL, 1),
                        Item(Tool.CATEGORY_ARTICLES, 1)
                    ),
                    selectedItem = Tool.CATEGORY_GOSPEL,
                    eventSink = events,
                ),
            )
        }
        composeTestRule.onNode(hasClickAction()).performClick()

        composeTestRule.onNodeWithText("Any category", substring = true, ignoreCase = true).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_FILTER_DROPDOWN).assertDoesNotExist()
        events.assertEvent(Event.SelectItem(null))
    }

    @Test
    fun `CategoryFilter() - Dropdown Menu - Select a category`() {
        composeTestRule.setContent {
            CategoryFilter(
                UiState(
                    items = persistentListOf(
                        Item(Tool.CATEGORY_GOSPEL, 1),
                        Item(Tool.CATEGORY_ARTICLES, 1)
                    ),
                    eventSink = events,
                ),
            )
        }
        composeTestRule.onNode(hasClickAction()).performClick()

        composeTestRule.onNodeWithText("Gospel", substring = true, ignoreCase = true).performClick()
        composeTestRule.onNodeWithTag(TEST_TAG_FILTER_DROPDOWN).assertDoesNotExist()
        events.assertEvent(Event.SelectItem(Tool.CATEGORY_GOSPEL))
    }
    // endregion CategoryFilter

    // region LanguageFilter
    @Test
    fun `LanguageFilter() - Shows selectedLanguage`() {
        composeTestRule.setContent {
            LanguageFilter(
                UiState(
                    selectedItem = Language(Locale.ENGLISH),
                    eventSink = events,
                )
            )
        }

        composeTestRule.onNodeWithText("English", substring = true, ignoreCase = true).assertExists()
        events.assertNoEvents()
    }

    @Test
    fun `LanguageFilter() - Shows Any Language when no language is specified`() {
        composeTestRule.setContent {
            LanguageFilter(
                UiState(
                    selectedItem = null,
                    eventSink = events,
                ),
            )
        }

        composeTestRule.onNodeWithText("Any language", substring = true, ignoreCase = true).assertExists()
        events.assertNoEvents()
    }

    @Test
    fun `LanguageFilter() - Button toggles menu`() {
        val menuExpanded = mutableStateOf(false)
        composeTestRule.setContent {
            LanguageFilter(
                UiState(
                    menuExpanded = menuExpanded,
                    selectedItem = Language(Locale.ENGLISH),
                    eventSink = events,
                ),
            )
        }

        // click button to show dropdown
        composeTestRule.onNodeWithText("English").assertHasClickAction().performClick()
        assertTrue(menuExpanded.value)
        events.assertNoEvents()
    }

    @Test
    fun `LanguageFilter() - Dropdown Menu - Show languages`() {
        composeTestRule.setContent {
            LanguageFilter(
                UiState(
                    menuExpanded = mutableStateOf(true),
                    items = persistentListOf(
                        Item(Language(Locale.FRENCH), 1),
                        Item(Language(Locale.GERMAN), 1),
                    ),
                    eventSink = events,
                ),
            )
        }

        composeTestRule.onNodeWithText("English", substring = true, ignoreCase = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("French", substring = true, ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("German", substring = true, ignoreCase = true).assertExists()
        events.assertNoEvents()
    }

    @Test
    fun `LanguageFilter() - Dropdown Menu - Select 'Any language' option`() {
        composeTestRule.setContent {
            LanguageFilter(
                UiState(
                    menuExpanded = mutableStateOf(true),
                    items = persistentListOf(
                        Item(Language(Locale.FRENCH), 1),
                        Item(Language(Locale.GERMAN), 1),
                    ),
                    selectedItem = Language(Locale.FRENCH),
                    eventSink = events,
                ),
            )
        }

        composeTestRule.onNodeWithText("Any language", substring = true, ignoreCase = true).performClick()
        events.assertEvent(Event.SelectItem(null))
    }

    @Test
    fun `LanguageFilter() - Dropdown Menu - Select a language`() {
        composeTestRule.setContent {
            LanguageFilter(
                UiState(
                    menuExpanded = mutableStateOf(true),
                    items = persistentListOf(
                        Item(Language(Locale.FRENCH), 1),
                        Item(Language(Locale.GERMAN), 1),
                    ),
                    eventSink = events,
                )
            )
        }

        composeTestRule.onNodeWithText("French", substring = true, ignoreCase = true).performClick()
        events.assertEvents(Event.SelectItem(Language(Locale.FRENCH)))
    }
    // endregion LanguageFilter
}
