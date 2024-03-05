package org.cru.godtools.ui.tooldetails

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import org.cru.godtools.ui.drawer.DrawerViewModel
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen.Event
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen.State
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolDetailsLayoutTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val events = TestEventSink<Event>()

    @BeforeTest
    fun setup() {
        // TODO: remove this once we migrate DrawerLayout to Circuit
        composeTestRule.activity.viewModelStore.put(
            "androidx.lifecycle.ViewModelProvider.DefaultKey:${DrawerViewModel::class.java.canonicalName}",
            DrawerViewModel(
                accountManager = mockk {
                    every { isAuthenticatedFlow } returns flowOf(false)
                }
            ),
        )
    }

    // region Action - Navigate Up
    @Test
    fun `Action - Navigate Up`() {
        val state = State(eventSink = events)
        composeTestRule.setContent { ToolDetailsLayout(state) }

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_NAVIGATE_UP).assertExists().performClick()
        events.assertEvent(Event.NavigateUp)
    }
    // endregion Action - Navigate Up

    // region Action - Pin Shortcut
    @Test
    fun `Action - Pin Shortcut`() {
        val state = State(hasShortcut = true, eventSink = events)
        composeTestRule.setContent { ToolDetailsLayout(state) }

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_PIN_SHORTCUT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_OVERFLOW).assertExists().performClick()
        events.assertNoEvents()

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_PIN_SHORTCUT).assertExists().performClick()
        events.assertEvent(Event.PinShortcut)
    }

    @Test
    fun `Action - Pin Shortcut - hasShortcut=false`() {
        val state = State(hasShortcut = false, eventSink = events)
        composeTestRule.setContent { ToolDetailsLayout(state) }

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_PIN_SHORTCUT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_OVERFLOW).assertDoesNotExist()
        events.assertNoEvents()
    }
    // endregion Action - Pin Shortcut

    // region Action - Tool Training Button
    @Test
    fun `Action - Tool Training Button - visible when manifest has tips`() {
        val state = State(
            manifest = mockk { every { hasTips } returns true },
            eventSink = events,
        )
        composeTestRule.setContent { ToolDetailsLayout(state) }

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_TOOL_TRAINING).assertExists().performClick()
        events.assertEvent(Event.OpenToolTraining)
    }

    @Test
    fun `Action - Tool Training Button - gone when manifest does not have tips`() {
        val state = State(
            manifest = mockk { every { hasTips } returns false },
            eventSink = events,
        )
        composeTestRule.setContent { ToolDetailsLayout(state) }

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_TOOL_TRAINING).assertDoesNotExist()
        events.assertNoEvents()
    }

    @Test
    fun `Action - Tool Training Button - gone when manifest does not exist`() {
        val state = State(manifest = null, eventSink = events)
        composeTestRule.setContent { ToolDetailsLayout(state) }

        composeTestRule.onNodeWithTag(TEST_TAG_ACTION_TOOL_TRAINING).assertDoesNotExist()
        events.assertNoEvents()
    }
    // endregion Action - Tool Training Button

    // region ToolDetailsLanguages()
    @Test
    fun `ToolDetailsLanguages() - No Languages`() {
        val state = State(availableLanguages = persistentListOf())
        composeTestRule.setContent { ToolDetailsLanguages(state, true, {}) }

        // The entire ToolDetailsLanguages() composable should be gone if there are no languages
        composeTestRule.onRoot().onChildren().assertCountEquals(0)
    }

    @Test
    fun `ToolDetailsLanguages() - Sorted Languages`() {
        val state = State(
            availableLanguages = persistentListOf("Language 1", "Language 2")
        )
        composeTestRule.setContent { ToolDetailsLanguages(state, true, {}) }

        composeTestRule.onNodeWithTag(TEST_TAG_LANGUAGES_AVAILABLE).assertTextEquals("Language 1, Language 2")
    }
    // endregion ToolDetailsLanguages()
}
