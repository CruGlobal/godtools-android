package org.cru.godtools.ui.dashboard.lessons

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LessonsViewModelTest {
    private val lessonsFlow = MutableStateFlow(emptyList<Tool>())

    private val toolsRepository: ToolsRepository = mockk {
        every { getLessonsFlow() } returns lessonsFlow
    }
    private val testScope = TestScope()

    private lateinit var viewModel: LessonsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher(testScope.testScheduler))
        viewModel = LessonsViewModel(mockk(), toolsRepository)
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Property lessons - Filter hidden lessons`() = testScope.runTest {
        val visible = Tool("visible", Tool.Type.LESSON)
        val hidden = Tool("hidden", Tool.Type.LESSON) { isHidden = true }

        viewModel.lessons.test {
            lessonsFlow.value = listOf(visible, hidden)
            runCurrent()
            assertThat(expectMostRecentItem(), contains("visible"))
        }
    }
}
