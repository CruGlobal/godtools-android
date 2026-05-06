package org.cru.godtools.ui.dashboard

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import app.cash.paparazzi.DeviceConfig
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.test.FakeImageLoaderEngine
import com.android.resources.NightMode
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.runtime.presenter.presenterOf
import io.mockk.mockk
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
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerPresenter
import org.cru.godtools.ui.dashboard.DashboardPresenter.UiState
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.home.HomeLayout
import org.cru.godtools.ui.dashboard.home.HomePresenter
import org.cru.godtools.ui.dashboard.lessons.LessonsLayout
import org.cru.godtools.ui.dashboard.lessons.LessonsPresenter
import org.cru.godtools.ui.dashboard.tools.ToolFiltersStateProducer.Filters
import org.cru.godtools.ui.dashboard.tools.ToolsLayout
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter
import org.cru.godtools.ui.tools.ToolCardStateTestData
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class DashboardLayoutPaparazziTest(
    @TestParameter(valuesProvider = DeviceConfigProvider::class) deviceConfig: DeviceConfig,
    @TestParameter("null", "in") locale: String?,
    @TestParameter nightMode: NightMode,
    @TestParameter accessibilityMode: AccessibilityMode,
) : BasePaparazziTest(
    deviceConfig = deviceConfig,
    locale = locale,
    nightMode = nightMode,
    accessibilityMode = accessibilityMode
) {
    private val state = UiState()

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

    // region ToolsLayout
    private var toolsState = ToolsPresenter.UiState(
        isPersonalizationEnabled = true,
        dataLoaded = true,
        spotlightTools = listOf(
            ToolCardStateTestData.tool.copy(
                toolCode = "spotlight1",
                tool = randomTool("spotlight1", isFavorite = false)
            ),
            ToolCardStateTestData.tool.copy(
                toolCode = "spotlight2",
                tool = randomTool("spotlight2", isFavorite = true)
            ),
            ToolCardStateTestData.tool.copy(
                toolCode = "spotlight3",
                tool = randomTool("spotlight3", isFavorite = false)
            ),
        ),
        tools = listOf(
            ToolCardStateTestData.tool.copy(toolCode = "tool1"),
            ToolCardStateTestData.tool.copy(toolCode = "tool2"),
            ToolCardStateTestData.tool.copy(toolCode = "tool3"),
        ),
    )

    @Test
    fun `ToolsLayout() - Data Not Loaded`() {
        assumeTrue(
            "Disable Accessibility screenshots since this doesn't have any addition",
            accessibilityMode == AccessibilityMode.NO_ACCESSIBILITY
        )
        assumeTrue(locale == null)
        toolsState = toolsState.copy(dataLoaded = false, tools = emptyList())
        snapshotDashboardLayout(state.copy(initialPage = ToolsScreen))
    }

    @Test
    fun `ToolsLayout() - Personalization`() {
        toolsState = toolsState.copy(mode = ToolsPresenter.UiState.Mode.PERSONALIZATION)
        snapshotDashboardLayout(state.copy(initialPage = ToolsScreen))
    }

    @Test
    fun `ToolsLayout() - Personalization - No Tools`() {
        assumeTrue(locale == null)
        toolsState = toolsState.copy(
            mode = ToolsPresenter.UiState.Mode.PERSONALIZATION,
            tools = emptyList()
        )
        snapshotDashboardLayout(state.copy(initialPage = ToolsScreen))
    }

    @Test
    fun `ToolsLayout() - All Tools`() {
        toolsState = toolsState.copy(
            mode = ToolsPresenter.UiState.Mode.ALL_TOOLS,
            spotlightTools = emptyList(),
        )
        snapshotDashboardLayout(state.copy(initialPage = ToolsScreen))
    }

    @Test
    fun `ToolsLayout() - All Tools - Filters Selected`() {
        assumeTrue(locale == null)
        toolsState = toolsState.copy(
            mode = ToolsPresenter.UiState.Mode.ALL_TOOLS,
            filters = Filters(
                categoryFilter = FilterMenu.UiState(selectedItem = Tool.CATEGORY_GOSPEL),
                languageFilter = FilterMenu.UiState(
                    selectedItem = Language(Locale.ENGLISH),
                    menuExpanded = mutableStateOf(false),
                ),
            ),
            spotlightTools = emptyList(),
        )
        snapshotDashboardLayout(state.copy(initialPage = ToolsScreen))
    }

    @Test
    @Ignore("LayoutLib does not correctly support Popups/Windows currently")
    fun `ToolsLayout() - All Tools - Language Filter Expanded`() {
        assumeTrue(locale == null)
        toolsState = toolsState.copy(
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
        snapshotDashboardLayout(state.copy(initialPage = ToolsScreen))
    }

    @Test
    fun `ToolsLayout() - No Personalization`() {
        assumeTrue(locale == null)
        toolsState = toolsState.copy(isPersonalizationEnabled = false)
        snapshotDashboardLayout(state.copy(initialPage = ToolsScreen))
    }
    // endregion ToolsLayout

    // region HomeLayout
    private var homeState = HomePresenter.UiState(
        dataLoaded = true,
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
    )

    @Test
    fun `HomeLayout()`() {
        assumeTrue(locale == null)
        snapshotDashboardLayout(state.copy(initialPage = HomeScreen))
    }

    @Test
    fun `HomeLayout() - Banner - Tutorial`() {
        assumeTrue(locale == null)
        homeState = homeState.copy(banner = TutorialFeaturesBannerPresenter.UiState())
        snapshotDashboardLayout(state.copy(initialPage = HomeScreen))
    }

    @Test
    fun `HomeLayout() - Data Not Loaded`() {
        assumeTrue(
            "Only do a single screenshot since this is currently a blank screen",
            deviceConfig == DeviceConfig.NEXUS_5 &&
                locale == null &&
                nightMode == NightMode.NOTNIGHT &&
                accessibilityMode == AccessibilityMode.NO_ACCESSIBILITY
        )
        homeState = homeState.copy(dataLoaded = false, favoriteTools = emptyList())
        snapshotDashboardLayout(state.copy(initialPage = HomeScreen))
    }

    @Test
    fun `HomeLayout() - No Spotlight Lessons`() {
        assumeTrue(locale == null)
        homeState = homeState.copy(spotlightLessons = emptyList())
        snapshotDashboardLayout(state.copy(initialPage = HomeScreen))
    }

    @Test
    fun `HomeLayout() - No Favorites`() {
        assumeTrue(locale == null)
        homeState = homeState.copy(favoriteTools = emptyList())
        snapshotDashboardLayout(state.copy(initialPage = HomeScreen))
    }
    // endregion HomeLayout

    // region LessonsLayout
    private val lessonsState = LessonsPresenter.UiState(
        languageFilter = FilterMenu.UiState(
            selectedItem = Language(Locale.ENGLISH)
        ),
        lessons = listOf(
            ToolCardStateTestData.tool.copy(toolCode = "lesson1", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "lesson2", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "lesson3", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "lesson4", translation = null),
            ToolCardStateTestData.tool.copy(toolCode = "lesson5", translation = null),
        )
    )

    @Test
    fun `LessonsLayout()`() {
        snapshotDashboardLayout(state.copy(initialPage = LessonsScreen))
    }
    // endregion LessonsLayout

    private val circuit = Circuit.Builder()
        .addPresenter<HomeScreen, HomePresenter.UiState> { _, _, _ -> presenterOf { homeState } }
        .addUi<HomeScreen, HomePresenter.UiState> { state, modifier -> HomeLayout(state, modifier) }
        .addPresenter<ToolsScreen, ToolsPresenter.UiState> { _, _, _ -> presenterOf { toolsState } }
        .addUi<ToolsScreen, ToolsPresenter.UiState> { state, modifier -> ToolsLayout(state, modifier) }
        .addPresenter<LessonsScreen, LessonsPresenter.UiState> { _, _, _ -> presenterOf { lessonsState } }
        .addUi<LessonsScreen, LessonsPresenter.UiState> { state, modifier -> LessonsLayout(state, modifier) }
        .build()

    private fun snapshotDashboardLayout(state: UiState = this.state) = snapshot {
        CircuitCompositionLocals(circuit) {
            CompositionLocalProvider(
                // mock required for AppUpdateSnackbar
                LocalActivityResultRegistryOwner provides mockk(relaxed = true),
                // mock required for AppUpdateSnackbar
                LocalAppUpdateManager provides mockk(relaxed = true)
            ) {
                DashboardLayout(state)
            }
        }
    }
}
