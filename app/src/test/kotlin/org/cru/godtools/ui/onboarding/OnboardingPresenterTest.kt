package org.cru.godtools.ui.onboarding

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.circuit.screen.AppLanguageScreen
import org.cru.godtools.shared.analytics.TutorialAnalyticsActionNames
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.greenrobot.eventbus.EventBus
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class OnboardingPresenterTest {
    private val eventBus: EventBus = mockk(relaxUnitFun = true)
    private val settings: Settings = mockk(relaxed = true)
    private val navigator = FakeNavigator(OnboardingScreen)

    private fun createPresenter() = OnboardingPresenter(
        eventBus = eventBus,
        settings = settings,
        navigator = navigator,
    )

    @AfterTest
    fun cleanup() {
        AndroidUiDispatcherUtil.runScheduledDispatches()
        navigator.assertGoToIsEmpty()
        navigator.assertPopIsEmpty()
    }

    // region Initial State
    @Test
    fun `Initial State - currentPage is 0`() = runTest {
        createPresenter().test {
            val state = awaitItem()
            assertEquals(0, state.pagerState.currentPage)
        }
    }

    @Test
    fun `Initial State - userScrollEnabled is false`() = runTest {
        createPresenter().test {
            val state = awaitItem()
            assertFalse(state.userScrollEnabled)
        }
    }
    // endregion Initial State

    // region Feature Discovery
    @Test
    fun `Feature Discovery - sets feature discovered`() = runTest {
        createPresenter().test {
            awaitItem()
            verify { settings.setFeatureDiscovered(Settings.FEATURE_TUTORIAL_ONBOARDING) }
        }
    }
    // endregion Feature Discovery

    // region UiEvent.ChangeLanguage
    @Test
    fun `UiEvent - ChangeLanguage`() = runTest {
        createPresenter().test {
            awaitItem().eventSink(OnboardingPresenter.UiEvent.ChangeLanguage)
            assertEquals(AppLanguageScreen, navigator.awaitNextScreen())
        }
    }
    // endregion UiEvent.ChangeLanguage

    // region UiEvent.Skip
    @Test
    fun `UiEvent - Skip`() = runTest {
        createPresenter().test {
            awaitItem().eventSink(OnboardingPresenter.UiEvent.Skip)
            verify { eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_SKIP)) }
            navigator.awaitPop()
        }
    }
    // endregion UiEvent.Skip

    // region UiEvent.Next
    @Test
    fun `UiEvent - Next - no language set - navigates to AppLanguageScreen`() = runTest {
        createPresenter().test {
            awaitItem().eventSink(OnboardingPresenter.UiEvent.Next)
            assertEquals(AppLanguageScreen, navigator.awaitNextScreen())
        }
    }

    @Test
    fun `UiEvent - Next - language already set - navigates to CountrySettingsScreen`() = runTest {
        // TODO: Testing the full answering navigator callback chain requires integration testing.
        //  The FakeNavigator doesn't trigger rememberAnsweringNavigator callbacks in unit tests.
    }
    // endregion UiEvent.Next

    // region UiEvent.Finish
    @Test
    fun `UiEvent - Finish`() = runTest {
        createPresenter().test {
            awaitItem().eventSink(OnboardingPresenter.UiEvent.Finish)
            verify { eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_FINISH)) }
            navigator.awaitPop()
        }
    }
    // endregion UiEvent.Finish
}
