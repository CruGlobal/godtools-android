package org.cru.godtools.ui.dashboard.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.test.FakeImageLoaderEngine
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.cru.godtools.ui.BasePaparazziTest
import org.cru.godtools.ui.banner.BannerType
import org.cru.godtools.ui.dashboard.home.HomeScreen.UiState
import org.cru.godtools.ui.tools.ToolCardStateTestData
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class HomeLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val state = UiState(
        spotlightLessons = listOf(
            ToolCardStateTestData.tool.copy(toolCode = "lesson", translation = null)
        ),
        favoriteTools = listOf(
            ToolCardStateTestData.tool.copy(toolCode = "tool1", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "tool2", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "tool3", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "tool4", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "tool5", translation = null),
        ),
        favoriteToolsLoaded = true
    )

    @BeforeTest
    @OptIn(ExperimentalCoilApi::class, ExperimentalCoroutinesApi::class)
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        Coil.setImageLoader(
            ImageLoader.Builder(paparazzi.context)
                .components {
                    add(
                        FakeImageLoaderEngine.Builder()
                            .intercept(ToolCardStateTestData.banner, ToolCardStateTestData.bannerDrawable)
                            .build()
                    )
                }
                .build()
        )
    }

    @AfterTest
    @OptIn(ExperimentalCoroutinesApi::class)
    fun cleanup() {
        Coil.reset()
        Dispatchers.resetMain()
    }

    @Test
    fun `HomeLayout()`() {
        snapshotHomeLayout(state)
    }

    @Test
    @Ignore("The Banner currently uses a ViewModel, which doesn't support Paparazzi")
    fun `HomeLayout() - Banner - Tutorial`() {
        snapshotHomeLayout(state.copy(banner = BannerType.TUTORIAL_FEATURES))
    }

    @Test
    fun `HomeLayout() - Favorites Not Loaded`() {
        snapshotHomeLayout(state.copy(favoriteTools = emptyList(), favoriteToolsLoaded = false))
    }

    @Test
    fun `HomeLayout() - No Favorites`() {
        snapshotHomeLayout(state.copy(favoriteTools = emptyList()))
    }

    @Test
    fun `HomeLayout() - No Spotlight Lessons`() {
        snapshotHomeLayout(state.copy(spotlightLessons = emptyList()))
    }

    private fun snapshotHomeLayout(state: UiState) {
        snapshot {
            HomeLayout(
                state,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}
