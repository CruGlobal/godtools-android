package org.cru.godtools.ui.dashboard.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
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
import kotlin.test.Ignore
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.cru.godtools.base.ui.BasePaparazziTest
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.tools.ToolFiltersStateProducer.Filters
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState
import org.cru.godtools.ui.tools.ToolCardStateTestData
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class ToolsLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(deviceConfig = deviceConfig, nightMode = nightMode, accessibilityMode = accessibilityMode) {
    private val tools = listOf(
        ToolCardStateTestData.tool.copy(toolCode = "tool1"),
        ToolCardStateTestData.tool.copy(toolCode = "tool2"),
        ToolCardStateTestData.tool.copy(toolCode = "tool3"),
    )
    private val spotlightTools = listOf(
        ToolCardStateTestData.tool.copy(toolCode = "spotlight1", tool = randomTool("spotlight1", isFavorite = false)),
        ToolCardStateTestData.tool.copy(toolCode = "spotlight2", tool = randomTool("spotlight2", isFavorite = true)),
        ToolCardStateTestData.tool.copy(toolCode = "spotlight3", tool = randomTool("spotlight3", isFavorite = false)),
    )

    private val state = UiState(
        dataLoaded = true,
        spotlightTools = spotlightTools,
        tools = tools,
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
    fun `ToolsLayout()`() = snapshotToolsLayout(state)

    @Test
    fun `ToolsLayout() - Data Not Loaded`() {
        assumeTrue(
            "Only do a single screenshot since this is currently a blank screen",
            deviceConfig == DeviceConfig.NEXUS_5 &&
                nightMode == NightMode.NOTNIGHT &&
                accessibilityMode == AccessibilityMode.NO_ACCESSIBILITY
        )
        snapshotToolsLayout(state.copy(dataLoaded = false, tools = emptyList()))
    }

    @Test
    fun `ToolsLayout() - No Tools`() = snapshotToolsLayout(state.copy(tools = emptyList()))

    @Test
    fun `ToolsLayout() - No Spotlight Tools`() = snapshotToolsLayout(state.copy(spotlightTools = emptyList()))

    @Test
    fun `ToolsLayout() - Filters Selected`() = snapshotToolsLayout(
        state.copy(
            filters = Filters(
                categoryFilter = FilterMenu.UiState(selectedItem = Tool.CATEGORY_GOSPEL),
                languageFilter = FilterMenu.UiState(
                    selectedItem = Language(Locale.ENGLISH),
                    menuExpanded = mutableStateOf(false),
                ),
            )
        )
    )

    @Test
    @Ignore("LayoutLib does not correctly support Popups/Windows currently")
    fun `ToolsLayout() - Language Filter Expanded`() = snapshotToolsLayout(
        state.copy(
            filters = Filters(
                languageFilter = FilterMenu.UiState(
                    selectedItem = Language(Locale.ENGLISH),
                    menuExpanded = mutableStateOf(true),
                    items = persistentListOf(
                        FilterMenu.UiState.Item(null, 0),
                        FilterMenu.UiState.Item(Language(Locale.ENGLISH), 12345),
                        FilterMenu.UiState.Item(Language(Locale.FRENCH), 1),
                        FilterMenu.UiState.Item(Language(Locale("es")), 3),
                    ),
                )
            )
        )
    )

    private fun snapshotToolsLayout(state: UiState) = snapshot {
        ToolsLayout(
            state,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    }
}
