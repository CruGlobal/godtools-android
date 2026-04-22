package org.cru.godtools.ui.banner.tutorial

import android.app.Application
import android.content.Context
import androidx.compose.runtime.withCompositionLocal
import androidx.compose.ui.platform.LocalContext
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.cru.godtools.base.Settings
import org.cru.godtools.ui.banner.Banner
import org.greenrobot.eventbus.EventBus
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class TutorialFeaturesBannerPresenterTest {
    private val featureDiscoveredFlow = MutableStateFlow(false)

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val eventBus: EventBus = mockk(relaxed = true)
    private val settings: Settings = mockk {
        every { isFeatureDiscoveredFlow(Settings.FEATURE_TUTORIAL_FEATURES) } returns featureDiscoveredFlow
        every { setFeatureDiscovered(any()) } just Runs
    }

    private val presenter = TutorialFeaturesBannerPresenter(eventBus, settings)

    // LocalContext is needed because the presenter captures it to start the tutorial activity on
    // OpenTutorial events. This is temporary — once the dashboard is fully migrated to Circuit,
    // navigation will go through the Circuit navigator instead of a raw context call.
    private fun presenterFlow() = moleculeFlow(RecompositionMode.Immediate) {
        withCompositionLocal(LocalContext provides context) { presenter.present() }
    }

    // region present()
    @Test
    fun `present - returns UiState when feature not yet discovered`() = runTest {
        presenterFlow().test {
            assertNotNull(awaitItem()) {
                assertEquals(Banner.Type.TUTORIAL_FEATURES, it.type)
            }
        }
    }

    @Test
    fun `present - returns null when feature is already discovered`() = runTest {
        featureDiscoveredFlow.value = true
        presenterFlow().test {
            assertNull(expectMostRecentItem())
        }
    }

    @Test
    fun `present - transitions to null when feature becomes discovered`() = runTest {
        presenterFlow().test {
            assertNotNull(awaitItem())

            featureDiscoveredFlow.value = true
            assertNull(awaitItem())
        }
    }
    // endregion present()

    // region UiEvent.Dismiss
    @Test
    fun `UiEvent - Dismiss - marks feature as discovered`() = runTest {
        presenterFlow().test {
            awaitItem()!!.eventSink(TutorialFeaturesBannerPresenter.UiEvent.Dismiss)
            verify { settings.setFeatureDiscovered(Settings.FEATURE_TUTORIAL_FEATURES) }
        }
    }

    @Test
    fun `UiEvent - Dismiss - posts analytics event`() = runTest {
        presenterFlow().test {
            awaitItem()!!.eventSink(TutorialFeaturesBannerPresenter.UiEvent.Dismiss)
            verify { eventBus.post(any()) }
        }
    }
    // endregion UiEvent.Dismiss
}
