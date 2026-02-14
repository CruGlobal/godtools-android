package org.cru.godtools.ui.languages.app

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import java.util.Locale
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import org.cru.godtools.ui.languages.app.AppLanguagePresenter.UiEvent
import org.cru.godtools.ui.languages.app.AppLanguagePresenter.UiState
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class AppLanguageLayoutTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<UiEvent>()

    @Test
    fun `Action - AppBar Navigate Back`() {
        composeTestRule.run {
            setContent { AppLanguageLayout(UiState(eventSink = events)) }
            onNodeWithTag(TEST_TAG_ACTION_BACK)
                .assertIsEnabled()
                .assertHasClickAction()
                .performClick()
        }

        events.assertEvent(UiEvent.NavigateBack)
    }

    @Test
    fun `Action - Select Language`() {
        composeTestRule.run {
            setContent {
                AppLanguageLayout(
                    UiState(
                        languages = persistentListOf(Locale.ENGLISH, Locale.FRENCH),
                        eventSink = events
                    )
                )
            }
            onNodeWithText("English", substring = true, ignoreCase = true)
                .assertExists()
                .performClick()
        }

        events.assertEvent(UiEvent.SelectLanguage(Locale.ENGLISH))
    }

    // region Search
    @Test
    fun `Search - Cancel Button not visible when not searching`() {
        composeTestRule.run {
            setContent {
                AppLanguageLayout(state = UiState(eventSink = events))
            }

            onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertDoesNotExist()
        }
    }

    @Test
    fun `Search - Cancel Button visible when searching`() {
        composeTestRule.run {
            setContent {
                AppLanguageLayout(
                    state = UiState(
                        languageQuery = remember { mutableStateOf("query") },
                        eventSink = events,
                    )
                )
            }

            onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertExists()
        }
    }
    // endregion Search

    // region Confirm Dialog
    @Test
    fun `Confirm Dialog - Action - Change Language button`() {
        composeTestRule.run {
            setContent {
                AppLanguageLayout(
                    UiState(
                        selectedLanguage = Locale.FRENCH,
                        eventSink = events
                    )
                )
            }
            events.assertNoEvents()

            onNodeWithText("Change Language", substring = true, ignoreCase = true)
                .assertExists()
                .performClick()
            events.assertEvent(UiEvent.ConfirmLanguage(Locale.FRENCH))
        }
    }

    @Test
    fun `Confirm Dialog - Action - Back button`() {
        composeTestRule.run {
            setContent {
                AppLanguageLayout(
                    UiState(
                        selectedLanguage = Locale.FRENCH,
                        eventSink = events
                    )
                )
            }
            events.assertNoEvents()

            Espresso.pressBack()
            events.assertEvent(UiEvent.DismissConfirmDialog)
        }
    }

    @Test
    fun `Confirm Dialog - Action - Nevermind button`() {
        composeTestRule.run {
            setContent {
                AppLanguageLayout(
                    UiState(
                        selectedLanguage = Locale.FRENCH,
                        eventSink = events
                    )
                )
            }
            events.assertNoEvents()

            onNodeWithText("Nevermind", substring = true, ignoreCase = true)
                .assertExists()
                .performClick()
            events.assertEvent(UiEvent.DismissConfirmDialog)
        }
    }
    // endregion Confirm Dialog
}
