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
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.ToolMatchers.tool
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
        every { primaryLanguage } returns Locale.ENGLISH
        every { primaryLanguageFlow } returns flowOf(Locale.ENGLISH)
        every { isFeatureDiscoveredFlow(any()) } returns flowOf(true)
    }
    private val testScope = TestScope()
    private val toolsRepository: ToolsRepository = mockk {
        every { getToolsFlow() } returns toolsFlow
        every { getMetaToolsFlow() } returns metaToolsFlow
    }

    private lateinit var viewModel: ToolsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher(testScope.testScheduler))
        viewModel = ToolsViewModel(
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

    // region Property filteredTools
    @Test
    fun `Property filteredTools - return only default variants`() = testScope.runTest {
        val meta = Tool("meta") {
            type = Tool.Type.META
            defaultVariantCode = "variant2"
        }
        val variant1 = Tool("variant1", metatool = "meta")
        val variant2 = Tool("variant2", metatool = "meta")

        viewModel.filteredTools.test {
            assertThat(awaitItem(), empty())

            toolsFlow.value = listOf(variant1, variant2)
            metaToolsFlow.value = listOf(meta)
            runCurrent()
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
        viewModel.filteredTools.test {
            assertThat(awaitItem(), empty())

            val hidden = Tool("hidden") {
                isHidden = true
            }
            val visible = Tool("visible")
            toolsFlow.value = listOf(hidden, visible)
            assertThat(awaitItem(), containsInAnyOrder(tool(visible)))
        }
    }
    // endregion Property filteredTools
}