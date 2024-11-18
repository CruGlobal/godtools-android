package org.cru.godtools.ui.dashboard.home

import androidx.compose.foundation.background
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
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.cru.godtools.ui.BasePaparazziTest
import org.cru.godtools.ui.dashboard.home.AllFavoritesScreen.UiState
import org.cru.godtools.ui.tools.ToolCardStateTestData
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class AllFavoritesLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val state = UiState(
        tools = listOf(
            ToolCardStateTestData.tool.copy(toolCode = "tool1", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "tool2", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "tool3", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "tool4", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "tool5", translation = null),
        )
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
    fun `AllFavoritesLayout()`() {
        snapshot {
            AllFavoritesLayout(state, modifier = Modifier.background(MaterialTheme.colorScheme.background))
        }
    }
}
