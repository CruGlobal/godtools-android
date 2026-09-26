package org.cru.godtools.tutorial.layout

import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithTag
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

    private fun ComposeUiTest.setTutorialLayoutContent(
        pageSet: PageSet,
        locale: () -> Locale = { Locale.ENGLISH },
    ) = setContent {
        CompositionLocalProvider(
            LocalAppLanguage provides locale(),
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

    @Test
    fun `Page Indicator - FEATURES - one indicator per page`() = runComposeUiTest {
        setTutorialLayoutContent(PageSet.FEATURES)

        val indicators = onAllNodesWithTag(TEST_TAG_PAGE_INDICATOR_PAGE)
        indicators.assertCountEquals(PageSet.FEATURES.pagesFor(Locale.ENGLISH).size)
        indicators[0].assertIsSelected()
        indicators[1].assertIsNotSelected()
    }

    @Test
    fun `Page Indicator - FEATURES - active indicator follows current page`() = runComposeUiTest {
        setTutorialLayoutContent(PageSet.FEATURES)
        onAllNodesWithTag(TEST_TAG_PAGE_INDICATOR_PAGE)[0].assertIsSelected()

        onNodeWithText(getString(R.string.tutorial_features_action_continue)).performClick()
        val indicators = onAllNodesWithTag(TEST_TAG_PAGE_INDICATOR_PAGE)
        indicators[0].assertIsNotSelected()
        indicators[1].assertIsSelected()
    }

    @Test
    fun `Page Indicator - FEATURES - updates when app language changes page count`() = runComposeUiTest {
        var locale by mutableStateOf(Locale.ENGLISH)
        setTutorialLayoutContent(PageSet.FEATURES) { locale }
        onAllNodesWithTag(TEST_TAG_PAGE_INDICATOR_PAGE)
            .assertCountEquals(PageSet.FEATURES.pagesFor(Locale.ENGLISH).size)

        locale = Locale.FRENCH
        val indicators = onAllNodesWithTag(TEST_TAG_PAGE_INDICATOR_PAGE)
        indicators.assertCountEquals(PageSet.FEATURES.pagesFor(Locale.FRENCH).size)
        indicators[0].assertIsSelected()
    }
    // endregion Page Indicator

    // region TutorialPagerIndicator()
    @Test
    fun `TutorialPagerIndicator() - clamps active indicator when page count shrinks`() = runComposeUiTest {
        var pageCount by mutableIntStateOf(5)
        setContent { TutorialPagerIndicator(rememberPagerState(initialPage = 4) { pageCount }) }
        onAllNodesWithTag(TEST_TAG_PAGE_INDICATOR_PAGE)[4].assertIsSelected()

        pageCount = 3
        val indicators = onAllNodesWithTag(TEST_TAG_PAGE_INDICATOR_PAGE)
        indicators.assertCountEquals(3)
        indicators[2].assertIsSelected()
    }
    // endregion TutorialPagerIndicator()
}
