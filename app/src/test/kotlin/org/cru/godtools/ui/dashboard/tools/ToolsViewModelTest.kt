package org.cru.godtools.ui.dashboard.tools

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.ToolMatchers.tool
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao

@OptIn(ExperimentalCoroutinesApi::class)
class ToolsViewModelTest {
    private val toolsFlow = MutableStateFlow(emptyList<Tool>())

    private val dao: GodToolsDao = mockk {
        every { getAsFlow<Tool>(any()) } returns flowOf(emptyList())
    }
    private val settings: Settings = mockk {
        every { primaryLanguage } returns Locale.ENGLISH
        every { primaryLanguageFlow } returns flowOf(Locale.ENGLISH)
        every { isFeatureDiscoveredFlow(any()) } returns flowOf(true)
    }
    private val testScope = TestScope()
    private val toolsRepository: ToolsRepository = mockk {
        every { getToolsFlow() } returns toolsFlow
    }

    private lateinit var viewModel: ToolsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher(testScope.testScheduler))
        viewModel = ToolsViewModel(
            dao = dao,
            eventBus = mockk(),
            settings = settings,
            toolsRepository = toolsRepository,
            savedState = SavedStateHandle()
        )
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    // region Property spotlightTools
    @Test
    fun `Property spotlightTools`() = testScope.runTest {
        viewModel.spotlightTools.test {
            assertThat(awaitItem(), empty())

            val normal = Tool("normal")
            val spotlight = Tool("spotlight") { isSpotlight = true }
            toolsFlow.value = listOf(normal, spotlight)
            assertThat(awaitItem(), containsInAnyOrder(tool(spotlight)))
        }
    }

    @Test
    fun `Property spotlightTools - Don't show hidden tools`() = testScope.runTest {
        viewModel.spotlightTools.test {
            assertThat(awaitItem(), empty())

            val hidden = Tool("normal") {
                isHidden = true
                isSpotlight = true
            }
            val spotlight = Tool("spotlight") { isSpotlight = true }
            toolsFlow.value = listOf(hidden, spotlight)
            assertThat(awaitItem(), containsInAnyOrder(tool(spotlight)))
        }
    }

    @Test
    fun `Property spotlightTools - Sorted by default order`() = testScope.runTest {
        viewModel.spotlightTools.test {
            assertThat(awaitItem(), empty())

            val tools = List(10) {
                Tool("tool$it") {
                    defaultOrder = Random.nextInt()
                    isSpotlight = true
                }
            }
            toolsFlow.value = tools
            assertThat(awaitItem(), contains(tools.sortedWith(Tool.COMPARATOR_DEFAULT_ORDER).map { tool(it) }))
        }
    }
    // endregion Property spotlightTools
}
