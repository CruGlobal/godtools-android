package org.cru.godtools.tool.tips.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TipBottomSheetDialogFragmentDataModelTest {
    private companion object {
        private const val TOOL = "tool"
        private const val TIP_ID_1 = "tipId1"
        private const val TIP_ID_2 = "tipId2"
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val tip1CompleteFlow = MutableSharedFlow<Boolean>(replay = 1).apply { tryEmit(false) }
    private val tip2CompleteFlow = MutableSharedFlow<Boolean>(replay = 1).apply { tryEmit(false) }

    private val manifestManager: ManifestManager = mockk()
    private val testScope = TestScope()
    private val tipsRepository: TrainingTipsRepository = mockk {
        every { isTipCompleteFlow(TOOL, Locale.ENGLISH, TIP_ID_1) } returns tip1CompleteFlow
        every { isTipCompleteFlow(TOOL, Locale.ENGLISH, TIP_ID_2) } returns tip2CompleteFlow
    }

    private lateinit var viewModel: TipBottomSheetDialogFragmentDataModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))
        viewModel = TipBottomSheetDialogFragmentDataModel(manifestManager, tipsRepository)
    }

    @Test
    fun testIsCompleted() = testScope.runTest {
        viewModel.isCompleted.test {
            assertFalse("isCompleted should be false because a valid tip isn't defined yet", expectMostRecentItem())

            viewModel.toolCode.value = TOOL
            viewModel.locale.value = Locale.ENGLISH
            viewModel.tipId.value = TIP_ID_1
            // We are now collecting tip1CompleteFlow, but it won't emit anything because the last emission was false
            expectNoEvents()

            tip1CompleteFlow.emit(true)
            assertTrue("isComplete should be true because tip1 is complete", expectMostRecentItem())
            tip1CompleteFlow.emit(false)
            assertFalse("isComplete should be false because tip1 is now not complete", expectMostRecentItem())
        }
        verifyAll {
            tipsRepository.isTipCompleteFlow(TOOL, Locale.ENGLISH, TIP_ID_1)
        }
    }

    @Test
    fun `testIsCompleted - switch tip complete flow`() = testScope.runTest {
        viewModel.toolCode.value = TOOL
        viewModel.locale.value = Locale.ENGLISH
        viewModel.tipId.value = TIP_ID_1
        tip1CompleteFlow.emit(true)

        viewModel.isCompleted.test {
            assertTrue("isComplete should be true because tip1 is complete", expectMostRecentItem())

            viewModel.tipId.value = TIP_ID_2
            tip2CompleteFlow.emit(false)
            assertFalse("isComplete should be false because tip2 is not complete", expectMostRecentItem())
        }
        verifyAll {
            tipsRepository.isTipCompleteFlow(TOOL, Locale.ENGLISH, TIP_ID_1)
            tipsRepository.isTipCompleteFlow(TOOL, Locale.ENGLISH, TIP_ID_2)
        }
    }
}
