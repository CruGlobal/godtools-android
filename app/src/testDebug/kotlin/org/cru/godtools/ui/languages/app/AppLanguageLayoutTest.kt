package org.cru.godtools.ui.languages.app

import android.app.Application
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
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class AppLanguageLayoutTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val events = TestEventSink<AppLanguageScreen.Event>()

    @Test
    fun `Action - AppBar Navigate Back`() {
        composeTestRule.run {
            setContent { AppLanguageLayout(AppLanguageScreen.State(eventSink = events)) }
            onNodeWithTag(TEST_TAG_ACTION_BACK)
                .assertIsEnabled()
                .assertHasClickAction()
                .performClick()
        }

        events.assertEvent(AppLanguageScreen.Event.NavigateBack)
    }

    @Test
    fun `Action - Select Language`() {
        composeTestRule.run {
            setContent {
                AppLanguageLayout(
                    AppLanguageScreen.State(
                        languages = persistentListOf(Locale.ENGLISH, Locale.FRENCH),
                        eventSink = events
                    )
                )
            }
            onNodeWithText("English", substring = true, ignoreCase = true)
                .assertExists()
                .performClick()
        }

        events.assertEvent(AppLanguageScreen.Event.SelectLanguage(Locale.ENGLISH))
    }

    // region Search
    @Test
    fun `Search - Cancel Button not visible when not searching`() {
        composeTestRule.run {
            setContent {
                AppLanguageLayout(state = AppLanguageScreen.State(languageQuery = "", eventSink = events))
            }

            onNodeWithTag(TEST_TAG_CANCEL_SEARCH).assertDoesNotExist()
        }
    }

    @Test
    fun `Search - Cancel Button visible when searching`() {
        composeTestRule.run {
            setContent {
                AppLanguageLayout(state = AppLanguageScreen.State(languageQuery = "query", eventSink = events))
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
                    AppLanguageScreen.State(
                        selectedLanguage = Locale.FRENCH,
                        eventSink = events
                    )
                )
            }
            events.assertNoEvents()

            onNodeWithText("Change Language", substring = true, ignoreCase = true)
                .assertExists()
                .performClick()
            events.assertEvent(AppLanguageScreen.Event.ConfirmLanguage(Locale.FRENCH))
        }
    }

    @Test
    fun `Confirm Dialog - Action - Back button`() {
        composeTestRule.run {
            setContent {
                AppLanguageLayout(
                    AppLanguageScreen.State(
                        selectedLanguage = Locale.FRENCH,
                        eventSink = events
                    )
                )
            }
            events.assertNoEvents()

            Espresso.pressBack()
            events.assertEvent(AppLanguageScreen.Event.DismissConfirmDialog)
        }
    }

    @Test
    fun `Confirm Dialog - Action - Nevermind button`() {
        composeTestRule.run {
            setContent {
                AppLanguageLayout(
                    AppLanguageScreen.State(
                        selectedLanguage = Locale.FRENCH,
                        eventSink = events
                    )
                )
            }
            events.assertNoEvents()

            onNodeWithText("Nevermind", substring = true, ignoreCase = true)
                .assertExists()
                .performClick()
            events.assertEvent(AppLanguageScreen.Event.DismissConfirmDialog)
        }
    }
    // endregion Confirm Dialog
}
