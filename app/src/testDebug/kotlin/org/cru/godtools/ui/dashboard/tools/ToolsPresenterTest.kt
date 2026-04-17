package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jeppeman.mockposable.mockk.everyComposable
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.ccci.gto.support.turbine.awaitItemMatching
import org.cru.godtools.base.CONFIG_UI_DASHBOARD_PERSONALIZATION_ENABLED
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.ui.banner.FakeBannerPresenter
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiEvent
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState.Mode
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Suppress("UnusedFlow")
@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ToolsPresenterTest {
    private var isPersonalizationEnabled = false
    private val toolsFlow = MutableStateFlow(emptyList<Tool>())
    private val filteredToolsFlow = MutableStateFlow(emptyList<Tool>())

    private val favoriteToolsBannerPresenter = FakeBannerPresenter<FavoriteToolsBannerPresenter.UiState>(null)
    private val filteredToolsFlowProducer: FilteredToolsFlowProducer = mockk {
        every { getFlow(any(), any(), any()) } returns filteredToolsFlow
    }
    private val navigator = FakeNavigator(ToolsScreen)
    private val remoteConfig: FirebaseRemoteConfig = mockk {
        every { getBoolean(CONFIG_UI_DASHBOARD_PERSONALIZATION_ENABLED) } answers { isPersonalizationEnabled }
    }
    private val toolsRepository: ToolsRepository = mockk {
        every { getNormalToolsFlow() } returns toolsFlow
    }
    private val toolFiltersStateProducer = FakeToolFiltersStateProducer()

    private val toolCardPresenter: ToolCardPresenter = mockk {
        everyComposable { present(tool = any(), secondLanguage = any(), eventSink = any()) }
            .answers { ToolCard.State(tool = firstArg()) }
    }

    private val presenter = ToolsPresenter(
        eventBus = mockk(),
        remoteConfig = remoteConfig,
        toolCardPresenter = toolCardPresenter,
        toolsRepository = toolsRepository,
        favoriteToolsBannerPresenter = favoriteToolsBannerPresenter,
        filteredToolsFlowProducer = filteredToolsFlowProducer,
        toolFiltersStateProducer = toolFiltersStateProducer,
        navigator = navigator,
    )

    @AfterTest
    fun cleanup() = AndroidUiDispatcherUtil.runScheduledDispatches()

    // region State.banner
    @Test
    fun `State - banner - none`() = runTest {
        favoriteToolsBannerPresenter.updateState(null)
        presenter.test {
            assertNull(expectMostRecentItem().banner)
        }
    }

    @Test
    fun `State - banner - favorites`() = runTest {
        val bannerState = FavoriteToolsBannerPresenter.UiState()
        favoriteToolsBannerPresenter.updateState(bannerState)
        presenter.test {
            assertEquals(bannerState, expectMostRecentItem().banner)
        }
    }
    // endregion State.banner

    // region State.mode
    @Test
    fun `State - mode - personalization enabled`() = runTest {
        isPersonalizationEnabled = true

        presenter.test {
            val state = awaitInitialItem()
            assertEquals(Mode.PERSONALIZATION, state.mode)

            state.eventSink(UiEvent.ChangeMode(Mode.ALL_TOOLS))
            assertEquals(Mode.ALL_TOOLS, awaitItem().mode)
        }
    }
    // endregion State.mode

    // region State.tools
    @Test
    fun `State - tools - shows tools from filteredToolsFlowProducer`() = runTest {
        val tool = randomTool(isHidden = false)
        filteredToolsFlow.value = listOf(tool)

        presenter.test {
            assertEquals(listOf(tool), awaitInitialItem().tools.map { it.tool })
        }
    }

    @Test
    fun `State - tools - updates when filtered tools change`() = runTest {
        val tool = randomTool(isHidden = false)

        presenter.test {
            assertEquals(emptyList(), awaitInitialItem().tools)

            filteredToolsFlow.value = listOf(tool)
            assertEquals(listOf(tool), awaitItem().tools.map { it.tool })
        }
    }

    @Test
    fun `State - tools - shows tools from correct mode flow`() = runTest {
        isPersonalizationEnabled = true
        val personalizationTools = List(2) { randomTool(isHidden = false) }
        val allToolsList = List(3) { randomTool(isHidden = false) }
        val personalizationFlow = MutableStateFlow(personalizationTools)
        val allToolsFlow = MutableStateFlow(allToolsList)
        every { filteredToolsFlowProducer.getFlow(Mode.PERSONALIZATION, any(), any()) } returns personalizationFlow
        every { filteredToolsFlowProducer.getFlow(Mode.ALL_TOOLS, any(), any()) } returns allToolsFlow

        presenter.test {
            val initial = awaitInitialItem()
            assertEquals(personalizationTools, initial.tools.map { it.tool })

            initial.eventSink(UiEvent.ChangeMode(Mode.ALL_TOOLS))
            assertEquals(allToolsList, awaitItemMatching { it.tools.size == allToolsList.size }.tools.map { it.tool })
        }
    }
    // endregion State.tools

    // region State.spotlightTools
    @Test
    fun `Property spotlightTools`() = runTest {
        val normalTool = randomTool("normal", isHidden = false, isSpotlight = false)
        val spotlightTool = randomTool("spotlight", isHidden = false, isSpotlight = true)

        presenter.test {
            toolsFlow.value = listOf(normalTool, spotlightTool)
            assertEquals(listOf(spotlightTool), expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }

    @Test
    fun `Property spotlightTools - Don't show hidden tools`() = runTest {
        val hiddenTool = randomTool("normal", isHidden = true, isSpotlight = true)
        val spotlightTool = randomTool("spotlight", isHidden = false, isSpotlight = true)

        presenter.test {
            toolsFlow.value = listOf(hiddenTool, spotlightTool)
            assertEquals(listOf(spotlightTool), expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }

    @Test
    fun `Property spotlightTools - Sorted by default order`() = runTest {
        val tools = List(10) {
            randomTool("tool$it", Tool.Type.TRACT, defaultOrder = it, isHidden = false, isSpotlight = true)
        }

        presenter.test {
            toolsFlow.value = tools.shuffled()
            assertEquals(tools, expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }

    @Test
    fun `Property spotlightTools - Don't show spotlight tools for ALL_TOOLS`() = runTest {
        isPersonalizationEnabled = true
        val normalTool = randomTool("normal", isHidden = false, isSpotlight = false)
        val spotlightTool = randomTool("spotlight", isHidden = false, isSpotlight = true)
        toolsFlow.value = listOf(normalTool, spotlightTool)

        presenter.test {
            val initialState = awaitInitialItem()
            assertEquals(listOf(spotlightTool), initialState.spotlightTools.map { it.tool })

            initialState.eventSink(UiEvent.ChangeMode(Mode.ALL_TOOLS))
            assertEquals(emptyList(), expectMostRecentItem().spotlightTools)
        }
    }
    // endregion State.spotlightTools

    // region State.filters
    @Test
    fun `State - filters - uses current mode`() = runTest {
        isPersonalizationEnabled = true

        presenter.test {
            assertEquals(Mode.PERSONALIZATION, toolFiltersStateProducer.lastMode)

            awaitInitialItem().eventSink(UiEvent.ChangeMode(Mode.ALL_TOOLS))
            awaitItem()
            assertEquals(Mode.ALL_TOOLS, toolFiltersStateProducer.lastMode)
        }
    }

    @Test
    @OptIn(ExperimentalUuidApi::class)
    fun `State - filters`() = runTest {
        val filters = ToolFiltersStateProducer.Filters(
            categoryFilter = FilterMenu.UiState(
                menuExpanded = mutableStateOf(Random.nextBoolean()),
                items = listOf(
                    FilterMenu.UiState.Item(null, 0),
                    FilterMenu.UiState.Item(Tool.CATEGORY_GOSPEL, 0),
                    FilterMenu.UiState.Item(Tool.CATEGORY_ARTICLES, 0),
                ),
                query = mutableStateOf(Uuid.random().toString()),
                selectedItem = null,
                eventSink = {},
            ),
            languageFilter = FilterMenu.UiState(
                menuExpanded = mutableStateOf(Random.nextBoolean()),
                items = listOf(
                    FilterMenu.UiState.Item(null, 0),
                    FilterMenu.UiState.Item(Language(Locale.ENGLISH), 0),
                    FilterMenu.UiState.Item(Language(Locale.FRENCH), 0),
                ),
                query = mutableStateOf(Uuid.random().toString()),
                selectedItem = null,
                eventSink = {},
            )
        )
        toolFiltersStateProducer.filters.value = filters

        presenter.test {
            assertEquals(filters, expectMostRecentItem().filters)
        }
    }
    // endregion State.filters

    private suspend fun ReceiveTurbine<UiState>.awaitInitialItem() = awaitItemMatching { it.dataLoaded }
}
