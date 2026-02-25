package org.cru.godtools.tutorial.layout

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.test.TestEventSink
import java.util.Locale
import kotlin.test.Test
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.base.ui.compose.LocalEventBus
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.layout.TutorialPresenter.UiEvent
import org.cru.godtools.tutorial.layout.TutorialPresenter.UiState
import org.greenrobot.eventbus.EventBus
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class TutorialLayoutTest {
    private val events = TestEventSink<UiEvent>()

    private fun getString(@StringRes resId: Int) =
        ApplicationProvider.getApplicationContext<Application>().getString(resId)

    private fun ComposeUiTest.setTutorialLayoutContent(pageSet: PageSet) = setContent {
        CompositionLocalProvider(
            LocalAppLanguage provides Locale.ENGLISH,
            LocalEventBus provides EventBus(),
        ) {
            ContentWithOverlays {
                TutorialLayout(UiState(pageSet, eventSink = events))
            }
        }
    }

    // region AppBar - Navigate Up
    @Test
    fun `AppBar - FEATURES - Navigate Up - click sends UiEvent Back`() = runComposeUiTest {
        setTutorialLayoutContent(PageSet.FEATURES)
        events.assertNoEvents()

        onNodeWithTag(TEST_TAG_NAVIGATE_UP).assertExists().performClick()
        events.assertEvent(UiEvent.Back)
    }
    // endregion AppBar - Navigate Up

    // region AppBar - Skip Menu
    @Test
    fun `AppBar - TIPS - Skip - click sends Tips Skip event`() = runComposeUiTest {
        setTutorialLayoutContent(PageSet.TIPS)
        events.assertNoEvents()

        onNodeWithText(getString(R.string.tutorial_tips_action_skip)).assertExists().performClick()
        events.assertEvent(UiEvent.Tips.Skip)
    }

    @Test
    fun `AppBar - LIVE_SHARE - Skip - click sends LiveShare Skip event`() = runComposeUiTest {
        setTutorialLayoutContent(PageSet.LIVE_SHARE)
        events.assertNoEvents()

        onNodeWithText(getString(R.string.tutorial_live_share_action_skip)).assertExists().performClick()
        events.assertEvent(UiEvent.LiveShare.Skip)
    }
    // endregion AppBar - Skip Menu

    // region Page Indicator
    @Test
    fun `Page Indicator - TIPS - visible`() = runComposeUiTest {
        setTutorialLayoutContent(PageSet.TIPS)
        onNodeWithTag(TEST_TAG_PAGE_INDICATOR).assertExists()
    }

    @Test
    fun `Page Indicator - LIVE_SHARE_PAGE_ONLY - not visible`() = runComposeUiTest {
        setTutorialLayoutContent(PageSet.LIVE_SHARE_START_PAGE_ONLY)
        onNodeWithTag(TEST_TAG_PAGE_INDICATOR).assertDoesNotExist()
    }
    // endregion Page Indicator
}
