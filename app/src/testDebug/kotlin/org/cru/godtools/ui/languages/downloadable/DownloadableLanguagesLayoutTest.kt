package org.cru.godtools.ui.languages.downloadable

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verifyAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.flow.MutableStateFlow
import org.cru.godtools.model.Language
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DownloadableLanguagesLayoutTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val searchQueryFlow = MutableStateFlow("")
    private val languagesFlow = MutableStateFlow(emptyList<Language>())
    private val languageViewModels: LanguageViewModels = mockk()
    private val viewModel: DownloadableLanguagesViewModel = mockk {
        every { searchQuery } returns searchQueryFlow
        every { updateSearchQuery(any()) } answers { searchQueryFlow.value = firstArg() }
        every { languages } returns languagesFlow
    }
    private val eventCallback: Function1<DownloadableLanguagesEvent, Unit> = mockk(relaxed = true) {
        excludeRecords { this@mockk.equals(any()) }
    }

    @Test
    fun `DownloadableLanguagesLayout() - Back navigation`() {
        composeTestRule.setContent {
            DownloadableLanguagesLayout(
                viewModel = viewModel,
                languageViewModels = languageViewModels,
                onEvent = eventCallback,
            )
        }

        assertFalse(composeTestRule.activity.isFinishing)
        Espresso.pressBack()
        assertTrue(composeTestRule.activity.isFinishing)
    }

    @Test
    fun `DownloadableLanguagesLayout() - Up navigation`() {
        composeTestRule.setContent {
            DownloadableLanguagesLayout(
                viewModel = viewModel,
                languageViewModels = languageViewModels,
                onEvent = eventCallback,
            )
        }

        searchQueryFlow.value = "ab"
        composeTestRule.onNodeWithTag(TEST_TAG_NAVIGATE_UP).performClick()
        composeTestRule.runOnIdle {
            verifyAll { eventCallback(DownloadableLanguagesEvent.NavigateUp) }
        }
    }

    @Test
    fun `DownloadableLanguagesLayout() - Search - Back Navigation`() {
        composeTestRule.setContent {
            DownloadableLanguagesLayout(
                viewModel = viewModel,
                languageViewModels = languageViewModels,
                onEvent = { fail() },
            )
        }

        searchQueryFlow.value = "search"
        composeTestRule.runOnIdle { Espresso.pressBack() }
        assertFalse(composeTestRule.activity.isFinishing)
        assertEquals("", searchQueryFlow.value)
        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertDoesNotExist()

        composeTestRule.runOnIdle { Espresso.pressBack() }
        assertTrue(composeTestRule.activity.isFinishing)
    }

    @Test
    fun `DownloadableLanguagesLayout() - Search - Cancel Button`() {
        composeTestRule.setContent {
            DownloadableLanguagesLayout(
                viewModel = viewModel,
                languageViewModels = languageViewModels,
                onEvent = { fail() },
            )
        }

        searchQueryFlow.value = "search"
        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH, useUnmergedTree = true).performClick()
        assertEquals("", searchQueryFlow.value)
        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertDoesNotExist()
    }

    @Test
    fun `DownloadableLanguagesLayout() - Search - Cancel Button only visible when searching`() {
        composeTestRule.setContent {
            DownloadableLanguagesLayout(
                viewModel = viewModel,
                languageViewModels = languageViewModels,
                onEvent = { fail() },
            )
        }

        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertDoesNotExist()
        searchQueryFlow.value = "search"
        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertExists().assertIsDisplayed()
        searchQueryFlow.value = ""
        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertDoesNotExist()
    }
}
