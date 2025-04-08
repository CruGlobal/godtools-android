package org.cru.godtools.ui.languages.downloadable

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.cru.godtools.ui.languages.downloadable.DownloadableLanguagesScreen.UiState
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DownloadableLanguagesLayoutTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val query = mutableStateOf("")

    private val eventSink = TestEventSink<UiState.UiEvent>()

    @Test
    fun `DownloadableLanguagesLayout() - Back navigation`() {
        composeTestRule.setContent {
            DownloadableLanguagesLayout(UiState())
        }

        assertFalse(composeTestRule.activity.isFinishing)
        Espresso.pressBack()
        assertTrue(composeTestRule.activity.isFinishing)
    }

    @Test
    fun `DownloadableLanguagesLayout() - Up navigation`() {
        composeTestRule.setContent {
            DownloadableLanguagesLayout(
                UiState(
                    query = remember { mutableStateOf("ab") },
                    eventSink = eventSink
                )
            )
        }

        eventSink.assertNoEvents()
        composeTestRule.onNodeWithTag(TEST_TAG_NAVIGATE_UP).performClick()
        eventSink.assertEvent(UiState.UiEvent.NavigateUp)
    }

    @Test
    fun `DownloadableLanguagesLayout() - Search - Back Navigation`() {
        composeTestRule.setContent {
            DownloadableLanguagesLayout(
                UiState(
                    query = query,
                    eventSink = eventSink
                )
            )
        }

        query.value = "search"
        composeTestRule.runOnIdle { Espresso.pressBack() }
        assertFalse(composeTestRule.activity.isFinishing)
        assertEquals("", query.value)
        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertDoesNotExist()

        composeTestRule.runOnIdle { Espresso.pressBack() }
        assertTrue(composeTestRule.activity.isFinishing)
    }

    @Test
    fun `DownloadableLanguagesLayout() - Search - Cancel Button`() {
        composeTestRule.setContent {
            DownloadableLanguagesLayout(
                UiState(
                    query = query,
                    eventSink = eventSink
                )
            )
        }

        query.value = "search"
        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH, useUnmergedTree = true).performClick()
        assertEquals("", query.value)
        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertDoesNotExist()
    }

    @Test
    fun `DownloadableLanguagesLayout() - Search - Cancel Button only visible when searching`() {
        composeTestRule.setContent {
            DownloadableLanguagesLayout(
                UiState(
                    query = query,
                    eventSink = eventSink
                )
            )
        }

        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertDoesNotExist()
        query.value = "search"
        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertExists().assertIsDisplayed()
        query.value = ""
        composeTestRule.onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertDoesNotExist()
    }
}
