package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.withCompositionLocal
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.InternalCircuitApi
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.ccci.gto.android.common.sync.SyncTaskRegistry
import org.ccci.gto.android.common.sync.SyncTracker
import org.ccci.gto.support.turbine.awaitItemMatching
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.banner.FakeBannerPresenter
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.personalization.LocalDashboardPersonalizationState
import org.cru.godtools.ui.dashboard.personalization.rememberDashboardPersonalizationState
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiEvent
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState.Mode
import org.cru.godtools.ui.tools.FakeToolCardPresenter
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ToolsPresenterTest {
    private var isPersonalizationEnabled = false
    private val appLocaleFlow = MutableStateFlow(Locale.ENGLISH)
    private val countryFlow = MutableStateFlow<String?>("US")
    private val personalizedFilterLocaleFlow = MutableStateFlow<Locale?>(null)
    private val featuredToolsFlow = MutableStateFlow(emptyList<Tool>())
    private val filteredToolsFlow = MutableStateFlow(emptyList<Tool>())
    private val toolOrderSync = Channel<Boolean>()

    private val testScope = TestScope()
    private val syncTaskRegistry = SyncTaskRegistry(SyncTracker(testScope.backgroundScope))
    @OptIn(InternalCircuitApi::class)
    private val circuitContext = CircuitContext(null).apply { putTag(syncTaskRegistry) }
    private val settings: Settings = mockk {
        every { appLanguageFlow } returns appLocaleFlow
        every { getCountrySettingFlow() } returns countryFlow
        every { getDashboardPersonalizedFilterLocaleFlow() } returns personalizedFilterLocaleFlow
    }
    private val languagesRepository: LanguagesRepository = mockk {
        every { findLanguageFlow(any()) } answers { flowOf(Language(firstArg())) }
    }
    private val syncService: GodToolsSyncService = mockk {
        coEvery { syncToolOrder(any(), any(), any()) } coAnswers { toolOrderSync.receive() }
        coEvery { syncFeaturedTools(any(), any(), any()) } returns true
    }
    private val favoriteToolsBannerPresenter = FakeBannerPresenter<FavoriteToolsBannerPresenter.UiState>(null)
    private val featuredToolsFlowProducer: FeaturedToolsFlowProducer = mockk {
        every { getFlow(any()) } returns featuredToolsFlow
    }
    private val filteredToolsFlowProducer: FilteredToolsFlowProducer = mockk {
        every { getFlow(any(), any(), any()) } returns filteredToolsFlow
    }
    private val navigator = FakeNavigator(ToolsScreen)
    private val toolFiltersStateProducer = FakeToolFiltersStateProducer()
    private val toolCardPresenter = FakeToolCardPresenter()

    private val toolsPresenter = ToolsPresenter(
        eventBus = mockk(),
        settings = settings,
        languagesRepository = languagesRepository,
        toolCardPresenter = toolCardPresenter,
        favoriteToolsBannerPresenter = favoriteToolsBannerPresenter,
        featuredToolsFlowProducer = featuredToolsFlowProducer,
        filteredToolsFlowProducer = filteredToolsFlowProducer,
        toolFiltersStateProducer = toolFiltersStateProducer,
        syncService = syncService,
        circuitContext = circuitContext,
        navigator = navigator,
    )

    // the page presenters read the personalization state that is normally provided by the dashboard
    private val presenter = object : Presenter<UiState> {
        @Composable
        override fun present(): UiState {
            val personalization = rememberDashboardPersonalizationState { isPersonalizationEnabled }
            return withCompositionLocal(LocalDashboardPersonalizationState provides personalization) {
                toolsPresenter.present()
            }
        }
    }

    @AfterTest
    fun cleanup() = AndroidUiDispatcherUtil.runScheduledDispatches()

    // region State.banner
    @Test
    fun `State - banner - none`() = testScope.runTest {
        favoriteToolsBannerPresenter.updateState(null)
        presenter.test {
            assertNull(expectMostRecentItem().banner)
        }
    }

    @Test
    fun `State - banner - favorites`() = testScope.runTest {
        val bannerState = FavoriteToolsBannerPresenter.UiState()
        favoriteToolsBannerPresenter.updateState(bannerState)
        presenter.test {
            assertEquals(bannerState, expectMostRecentItem().banner)
        }
    }
    // endregion State.banner

    // region State.mode
    @Test
    fun `State - mode - personalization enabled`() = testScope.runTest {
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
    fun `State - tools - shows tools from filteredToolsFlowProducer`() = testScope.runTest {
        val tool = randomTool(isHidden = false)
        filteredToolsFlow.value = listOf(tool)

        presenter.test {
            assertEquals(listOf(tool), awaitInitialItem().tools.map { it.tool })
        }
    }

    @Test
    fun `State - tools - updates when filtered tools change`() = testScope.runTest {
        val tool = randomTool(isHidden = false)

        presenter.test {
            assertEquals(emptyList(), awaitInitialItem().tools)

            filteredToolsFlow.value = listOf(tool)
            assertEquals(listOf(tool), awaitItem().tools.map { it.tool })
        }
    }

    @Test
    fun `State - tools - shows tools from correct mode flow`() = testScope.runTest {
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
    fun `State - spotlightTools - shows tools from featuredToolsFlowProducer`() = testScope.runTest {
        isPersonalizationEnabled = true
        val tool = randomTool(isHidden = false)

        presenter.test {
            featuredToolsFlow.value = listOf(tool)
            assertEquals(listOf(tool), expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }

    @Test
    fun `State - spotlightTools - uses the app language when no personalized filter language is stored`() =
        testScope.runTest {
            isPersonalizationEnabled = true
            appLocaleFlow.value = Locale.GERMAN

            presenter.test {
                awaitInitialItem()
                toolOrderSync.send(true)
                verify { featuredToolsFlowProducer.getFlow(Locale.GERMAN) }
                coVerify { syncService.syncFeaturedTools(Locale.GERMAN, any(), any()) }
            }
        }

    @Test
    fun `State - spotlightTools - uses the personalized filter language instead of the app language`() =
        testScope.runTest {
            isPersonalizationEnabled = true
            appLocaleFlow.value = Locale.GERMAN
            personalizedFilterLocaleFlow.value = Locale.FRENCH
            val tool = randomTool(isHidden = false)
            every { featuredToolsFlowProducer.getFlow(Locale.FRENCH) } returns MutableStateFlow(listOf(tool))

            presenter.test {
                assertEquals(listOf(tool), expectMostRecentItem().spotlightTools.map { it.tool })
                toolOrderSync.send(true)
                verify(exactly = 0) { featuredToolsFlowProducer.getFlow(Locale.GERMAN) }
                coVerify { syncService.syncFeaturedTools(Locale.FRENCH, any(), any()) }
            }
        }

    @Test
    fun `State - spotlightTools - cards show the app language when no personalized filter language is stored`() =
        testScope.runTest {
            isPersonalizationEnabled = true
            featuredToolsFlow.value = listOf(randomTool(isHidden = false))

            presenter.test {
                assertEquals(
                    listOf(Locale.ENGLISH),
                    expectMostRecentItem().spotlightTools.map { it.secondLanguage?.code },
                )
            }
        }

    @Test
    fun `State - spotlightTools - cards show the personalized filter language`() = testScope.runTest {
        isPersonalizationEnabled = true
        personalizedFilterLocaleFlow.value = Locale.FRENCH
        featuredToolsFlow.value = listOf(randomTool(isHidden = false))

        presenter.test {
            assertEquals(listOf(Locale.FRENCH), expectMostRecentItem().spotlightTools.map { it.secondLanguage?.code })
        }
    }

    @Test
    fun `State - spotlightTools - Don't show featured tools for ALL_TOOLS`() = testScope.runTest {
        isPersonalizationEnabled = true
        val featuredTool = randomTool(isHidden = false)
        featuredToolsFlow.value = listOf(featuredTool)

        presenter.test {
            val initialState = awaitInitialItem()
            assertEquals(listOf(featuredTool), initialState.spotlightTools.map { it.tool })

            initialState.eventSink(UiEvent.ChangeMode(Mode.ALL_TOOLS))
            assertEquals(emptyList(), expectMostRecentItem().spotlightTools)
        }
    }

    @Test
    fun `State - spotlightTools - Don't show featured tools when personalization is disabled`() = testScope.runTest {
        isPersonalizationEnabled = false
        featuredToolsFlow.value = listOf(randomTool(isHidden = false))

        presenter.test {
            assertEquals(emptyList(), awaitInitialItem().spotlightTools)
        }
    }
    // endregion State.spotlightTools

    // region State.filters
    @Test
    fun `State - filters - uses current mode`() = testScope.runTest {
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
    fun `State - filters`() = testScope.runTest {
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

    // region SideEffect - RegisterSyncTask
    @Test
    fun `SideEffect - RegisterSyncTask - Triggers initial sync`() = testScope.runTest {
        presenter.test {
            awaitInitialItem()
            toolOrderSync.send(true)
            coVerifyAll {
                syncService.syncToolOrder(Locale.ENGLISH, "US", false)
                syncService.syncFeaturedTools(Locale.ENGLISH, "US", false)
            }
        }
    }

    @Test
    fun `SideEffect - RegisterSyncTask - skips featured tools sync without a country`() = testScope.runTest {
        countryFlow.value = null

        presenter.test {
            awaitInitialItem()
            toolOrderSync.send(true)
            coVerifyAll { syncService.syncToolOrder(Locale.ENGLISH, null, false) }
        }
    }

    @Test
    fun `SideEffect - RegisterSyncTask - tool order uses language filter, featured tools ignore it`() =
        testScope.runTest {
            toolFiltersStateProducer.filters.value = ToolFiltersStateProducer.Filters(
                languageFilter = FilterMenu.UiState(selectedItem = Language(Locale.FRENCH))
            )

            presenter.test {
                awaitInitialItem()
                toolOrderSync.send(true)
                coVerifyAll {
                    syncService.syncToolOrder(Locale.FRENCH, "US", false)
                    syncService.syncFeaturedTools(Locale.ENGLISH, "US", false)
                }
            }
        }

    @Test
    fun `SideEffect - RegisterSyncTask - re-syncs when locale changes`() = testScope.runTest {
        presenter.test {
            awaitInitialItem()
            toolOrderSync.send(true)
            coVerify { syncService.syncToolOrder(Locale.ENGLISH, "US", false) }

            toolFiltersStateProducer.filters.value = ToolFiltersStateProducer.Filters(
                languageFilter = FilterMenu.UiState(selectedItem = Language(Locale.FRENCH))
            )
            awaitItemMatching { it.filters.languageFilter.selectedItem?.code == Locale.FRENCH }
            toolOrderSync.send(true)
            coVerify { syncService.syncToolOrder(Locale.FRENCH, "US", false) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SideEffect - RegisterSyncTask - passes force on triggered sync`() = testScope.runTest {
        presenter.test {
            awaitInitialItem()
            toolOrderSync.send(true)
            coVerify { syncService.syncToolOrder(Locale.ENGLISH, "US", false) }

            syncTaskRegistry.triggerSyncTasks(force = true)
            toolOrderSync.send(true)
            coVerify { syncService.syncToolOrder(Locale.ENGLISH, "US", true) }
        }
    }
    // endregion SideEffect - RegisterSyncTask

    private suspend fun ReceiveTurbine<UiState>.awaitInitialItem() = awaitItemMatching { it.dataLoaded }
}
