package org.cru.godtools.ui.languages

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher.Companion.expectValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import java.util.Locale
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.cru.godtools.base.ui.compose.LocalEventBus
import org.cru.godtools.ui.drawer.putDrawerViewModel
import org.cru.godtools.ui.languages.LanguageSettingsScreen.Event
import org.cru.godtools.ui.languages.LanguageSettingsScreen.State
import org.greenrobot.eventbus.EventBus
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class LanguageSettingsLayoutTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val events = TestEventSink<Event>()
    private val state = State(appLanguage = Locale.ENGLISH, eventSink = events)

    @BeforeTest
    fun setup() {
        // TODO: remove this once we migrate DrawerLayout to Circuit
        composeTestRule.activity.viewModelStore.putDrawerViewModel()
    }

    @Test
    fun `Action - AppBar - Navigate Up`() {
        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalEventBus provides EventBus()) {
                    LanguageSettingsLayout(state)
                }
            }
            onNodeWithTag(TEST_TAG_ACTION_BACK)
                .assertIsEnabled()
                .assertHasClickAction()
                .performClick()
        }

        events.assertEvent(Event.NavigateUp)
    }

    @Test
    fun `Action - Button - App Language`() {
        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalEventBus provides EventBus()) {
                    LanguageSettingsLayout(state)
                }
            }
            onNode(expectValue(SemanticsProperties.Role, Role.Button) and hasText("English"))
                .assertIsEnabled()
                .assertHasClickAction()
                .performClick()
        }

        events.assertEvent(Event.AppLanguage)
    }

    @Test
    fun `Action - Button - Downloadable Languages`() {
        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalEventBus provides EventBus()) {
                    LanguageSettingsLayout(state)
                }
            }
            onNode(expectValue(SemanticsProperties.Role, Role.Button) and hasText("Edit downloaded languages"))
                .assertIsEnabled()
                .assertHasClickAction()
                .performClick()
        }

        events.assertEvent(Event.DownloadableLanguages)
    }
}
