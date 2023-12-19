package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.cru.godtools.TestUtils.clearAndroidUiDispatcher
import org.cru.godtools.base.Settings
import org.cru.godtools.ui.banner.BannerType
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolsPresenterTest {
    private val isFavoritesFeatureDiscovered = MutableStateFlow(true)

    private val navigator = FakeNavigator()
    private val settings: Settings = mockk {
        every { isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE) } returns isFavoritesFeatureDiscovered
    }

    private val presenter = ToolsPresenter(
        settings = settings,
        navigator = navigator,
    )

    @AfterTest
    fun cleanup() = clearAndroidUiDispatcher()

    // region State.banner
    @Test
    fun `State - banner - none`() = runTest {
        presenterTestOf(
            presentFunction = {
                ToolsScreen.State(banner = presenter.rememberBanner(), eventSink = {})
            }
        ) {
            isFavoritesFeatureDiscovered.value = true
            assertNull(expectMostRecentItem().banner)
        }
    }

    @Test
    fun `State - banner - favorites`() = runTest {
        presenterTestOf(
            presentFunction = {
                ToolsScreen.State(banner = presenter.rememberBanner(), eventSink = {})
            }
        ) {
            isFavoritesFeatureDiscovered.value = false
            assertEquals(BannerType.TOOL_LIST_FAVORITES, expectMostRecentItem().banner)
        }
    }
    // endregion State.banner
}
