package org.cru.godtools.ui.dashboard.tools

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.ToolMatchers.tool
import org.cru.godtools.model.randomTool
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ToolsViewModelTest {
    private val toolsFlow = MutableStateFlow(emptyList<Tool>())
    private val metaToolsFlow = MutableStateFlow(emptyList<Tool>())

    private val settings: Settings = mockk {
        every { appLanguageFlow } returns flowOf(Locale.ENGLISH)
        every { isFeatureDiscoveredFlow(any()) } returns flowOf(true)
    }
    private val testScope = TestScope()
    private val toolsRepository: ToolsRepository = mockk {
        every { getNormalToolsFlow() } returns toolsFlow
        every { getMetaToolsFlow() } returns metaToolsFlow
    }

    private lateinit var viewModel: ToolsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))
        viewModel = ToolsViewModel(
            context = mockk(),
            eventBus = mockk(),
            settings = settings,
            languagesRepository = mockk(),
            toolsRepository = toolsRepository,
            savedState = SavedStateHandle(),
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

            val normal = randomTool("normal", isHidden = false, isSpotlight = false)
            val spotlight = randomTool("spotlight", isHidden = false, isSpotlight = true)
            toolsFlow.value = listOf(normal, spotlight)
            assertThat(awaitItem(), containsInAnyOrder(tool(spotlight)))
        }
    }

    @Test
    fun `Property spotlightTools - Don't show hidden tools`() = testScope.runTest {
        viewModel.spotlightTools.test {
            assertThat(awaitItem(), empty())

            val hidden = randomTool("normal", isHidden = true, isSpotlight = true)
            val spotlight = randomTool("spotlight", isHidden = false, isSpotlight = true)
            toolsFlow.value = listOf(hidden, spotlight)
            assertThat(awaitItem(), containsInAnyOrder(tool(spotlight)))
        }
    }

    @Test
    fun `Property spotlightTools - Sorted by default order`() = testScope.runTest {
        viewModel.spotlightTools.test {
            assertThat(awaitItem(), empty())

            val tools = List(10) { randomTool("tool$it", Tool.Type.TRACT, isHidden = false, isSpotlight = true) }
            toolsFlow.value = tools
            assertThat(awaitItem(), contains(tools.sortedWith(Tool.COMPARATOR_DEFAULT_ORDER).map { tool(it) }))
        }
    }
    // endregion Property spotlightTools

    // region Property filteredTools
    @Test
    fun `Property filteredTools - return only default variants`() = testScope.runTest {
        val meta = Tool("meta", Tool.Type.META, defaultVariantCode = "variant2")
        val variant1 = Tool("variant1", metatoolCode = "meta")
        val variant2 = Tool("variant2", metatoolCode = "meta")

        viewModel.tools.test {
            assertThat(awaitItem(), empty())

            toolsFlow.value = listOf(variant1, variant2)
            metaToolsFlow.value = listOf(meta)
            assertThat(
                expectMostRecentItem(),
                allOf(
                    contains(tool(variant2)),
                    not(hasItem(tool(meta))),
                    not(hasItem(tool(variant1)))
                )
            )
        }
    }

    @Test
    fun `Property filteredTools - Don't show hidden tools`() = testScope.runTest {
        viewModel.tools.test {
            assertThat(awaitItem(), empty())

            val hidden = randomTool("hidden", isHidden = true, metatoolCode = null)
            val visible = randomTool("visible", isHidden = false, metatoolCode = null)
            toolsFlow.value = listOf(hidden, visible)
            assertThat(awaitItem(), containsInAnyOrder(tool(visible)))
        }
    }
    // endregion Property filteredTools
}
