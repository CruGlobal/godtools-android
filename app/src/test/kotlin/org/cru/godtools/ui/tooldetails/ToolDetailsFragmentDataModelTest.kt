package org.cru.godtools.ui.tooldetails

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.ToolMatchers.tool
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.junit.After
import org.junit.Before
import org.junit.Test

private const val TOOL = "tool"

@OptIn(ExperimentalCoroutinesApi::class)
class ToolDetailsFragmentDataModelTest {
    private val toolFlow = MutableStateFlow<Tool?>(null)
    private val toolsFlow = MutableStateFlow(emptyList<Tool>())

    private val toolsRepository: ToolsRepository = mockk {
        every { findToolFlow(any()) } returns toolFlow
        every { getToolsFlow() } returns toolsFlow
    }
    private val testScope = TestScope()

    private lateinit var viewModel: ToolDetailsFragmentDataModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher(testScope.testScheduler))
        viewModel = ToolDetailsFragmentDataModel(
            attachmentsRepository = mockk(),
            shortcutManager = mockk(),
            toolFileSystem = mockk(),
            toolsRepository = toolsRepository,
            savedStateHandle = SavedStateHandle()
        )
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Property variants`() = testScope.runTest {
        val tool = Tool(TOOL, metatool = "meta")
        val variant1 = Tool("variant1", metatool = "meta")
        val tool2 = Tool("tool2")
        viewModel.setToolCode(TOOL)
        toolsFlow.value = listOf(tool, tool2, variant1)

        viewModel.variants.test {
            assertThat(awaitItem(), empty())

            toolFlow.value = tool
            advanceUntilIdle()
            assertThat(awaitItem(), containsInAnyOrder(tool(tool), tool(variant1)))

            toolFlow.value = variant1
            advanceUntilIdle()
            expectNoEvents()

            toolFlow.value = tool2
            advanceUntilIdle()
            assertThat(awaitItem(), empty())
        }
    }
}
