package org.cru.godtools.ui.banner.favoritetools

import android.app.Application
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
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class FavoriteToolsBannerPresenterTest {
    private val featureDiscoveredFlow = MutableStateFlow(false)
    private val settings: Settings = mockk {
        every { isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE) } returns featureDiscoveredFlow
        every { setFeatureDiscovered(any()) } just Runs
    }

    private val presenter = FavoriteToolsBannerPresenter(settings)

    private fun presenterFlow() = moleculeFlow(RecompositionMode.Immediate) { presenter.present() }

    // region present()
    @Test
    fun `present - returns UiState when feature not yet discovered`() = runTest {
        presenterFlow().test {
            assertNotNull(awaitItem()) {
                assertEquals(Banner.Type.TOOL_LIST_FAVORITES, it.type)
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
            awaitItem()!!.eventSink(FavoriteToolsBannerPresenter.UiEvent.Dismiss)
            verify { settings.setFeatureDiscovered(Settings.FEATURE_TOOL_FAVORITE) }
        }
    }
    // endregion UiEvent.Dismiss
}
