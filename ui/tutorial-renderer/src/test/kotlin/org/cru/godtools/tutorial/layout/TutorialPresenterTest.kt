package org.cru.godtools.tutorial.layout

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.cru.godtools.base.Settings
import org.cru.godtools.tutorial.PageSet
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class TutorialPresenterTest {
    private val settings: Settings = mockk(relaxed = true)
    private val navigator = FakeNavigator(TutorialScreen(PageSet.FEATURES))

    private fun createPresenter(pageSet: PageSet = PageSet.FEATURES) = TutorialPresenter(
        settings = settings,
        navigator = navigator,
        screen = TutorialScreen(pageSet),
    )

    @AfterTest
    fun cleanup() {
        AndroidUiDispatcherUtil.runScheduledDispatches()
        navigator.assertGoToIsEmpty()
        navigator.assertPopIsEmpty()
    }

    // region UiState.pageSet
    @Test
    fun `UiState - pageSet`() = runTest {
        createPresenter(PageSet.FEATURES).test {
            assertEquals(PageSet.FEATURES, awaitItem().pageSet)
        }
    }
    // endregion State

    // region Feature Discovery
    @Test
    fun `Feature Discovery - FEATURES - sets feature discovered`() = runTest {
        createPresenter(PageSet.FEATURES).test {
            awaitItem()
            verify { settings.setFeatureDiscovered(Settings.FEATURE_TUTORIAL_FEATURES) }
        }
    }

    @Test
    fun `Feature Discovery - LIVE_SHARE - no feature discovered`() = runTest {
        createPresenter(PageSet.LIVE_SHARE).test {
            awaitItem()
            verify(exactly = 0) { settings.setFeatureDiscovered(any()) }
        }
    }
    // endregion Feature Discovery

    // region UiEvent.Back
    @Test
    fun `UiEvent - Back`() = runTest {
        createPresenter().test {
            awaitItem().eventSink(TutorialPresenter.UiEvent.Back)
            assertEquals(TutorialScreen.Result.Canceled, navigator.awaitPop().result)
        }
    }
    // endregion UiEvent.Back

    // region UiEvent.LiveShare
    @Test
    fun `UiEvent - LiveShare - QrCode`() = runTest {
        createPresenter(PageSet.LIVE_SHARE).test {
            awaitItem().eventSink(TutorialPresenter.UiEvent.LiveShare.QrCode)
            assertEquals(TutorialScreen.Result.ShowQrCode, navigator.awaitPop().result)
        }
    }

    @Test
    fun `UiEvent - LiveShare - Skip`() = runTest {
        createPresenter(PageSet.LIVE_SHARE).test {
            awaitItem().eventSink(TutorialPresenter.UiEvent.LiveShare.Skip)
            assertEquals(TutorialScreen.Result.Finished, navigator.awaitPop().result)
        }
    }

    @Test
    fun `UiEvent - LiveShare - Finish`() = runTest {
        createPresenter(PageSet.LIVE_SHARE).test {
            awaitItem().eventSink(TutorialPresenter.UiEvent.LiveShare.Finish)
            assertEquals(TutorialScreen.Result.Finished, navigator.awaitPop().result)
        }
    }
    // endregion UiEvent.LiveShare

    // region UiEvent.Features
    @Test
    fun `UiEvent - Features - Finish`() = runTest {
        createPresenter(PageSet.FEATURES).test {
            awaitItem().eventSink(TutorialPresenter.UiEvent.Features.Finish)
            assertEquals(TutorialScreen.Result.Finished, navigator.awaitPop().result)
        }
    }
    // endregion UiEvent.Features

    // region UiEvent.Tips
    @Test
    fun `UiEvent - Tips - Skip`() = runTest {
        createPresenter(PageSet.TIPS).test {
            awaitItem().eventSink(TutorialPresenter.UiEvent.Tips.Skip)
            assertEquals(TutorialScreen.Result.Finished, navigator.awaitPop().result)
        }
    }

    @Test
    fun `UiEvent - Tips - Finish`() = runTest {
        createPresenter(PageSet.TIPS).test {
            awaitItem().eventSink(TutorialPresenter.UiEvent.Tips.Finish)
            assertEquals(TutorialScreen.Result.Finished, navigator.awaitPop().result)
        }
    }
    // endregion UiEvent.Tips
}
