package org.cru.godtools.ui.dashboard.lessons

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
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.cru.godtools.model.Language
import org.cru.godtools.ui.BasePaparazziTest
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.lessons.LessonsScreen.UiState
import org.cru.godtools.ui.tools.ToolCardStateTestData
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class LessonsLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val state = UiState(
        languageFilter = FilterMenu.UiState(
            selectedItem = Language(Locale.ENGLISH)
        ),
        lessons = persistentListOf(
            ToolCardStateTestData.tool.copy(toolCode = "lesson1", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "lesson2", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "lesson3", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "lesson4", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "lesson5", translation = null),
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
    fun `LessonsLayout()`() {
        snapshot {
            LessonsLayout(state, modifier = Modifier.background(MaterialTheme.colorScheme.background))
        }
    }
}
