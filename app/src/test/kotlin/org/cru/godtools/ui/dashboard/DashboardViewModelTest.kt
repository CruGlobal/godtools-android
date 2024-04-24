package org.cru.godtools.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.sync.GodToolsSyncService

@Suppress("DeferredResultUnused")
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val syncService: GodToolsSyncService = mockk {
        every { syncFollowupsAsync() } returns CompletableDeferred()
        every { syncToolSharesAsync() } returns CompletableDeferred()
        coEvery { syncTools(any()) } returns true
        coEvery { syncFavoriteTools(any()) } returns true
    }
    private val testScope = TestScope()

    private lateinit var viewModel: DashboardViewModel

    @BeforeTest
    fun createViewModel() {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))

        viewModel = DashboardViewModel(
            syncService = syncService,
            savedState = SavedStateHandle()
        )
    }

    @AfterTest
    fun reset() {
        Dispatchers.resetMain()
    }

    @Test
    fun `triggerSync() - Initial sync`() = testScope.runTest {
        coVerifyAll {
            syncService.syncFollowupsAsync()
            syncService.syncToolSharesAsync()
            syncService.syncTools(false)
            syncService.syncFavoriteTools(false)
        }
    }

    @Test
    fun `triggerSync() - isSyncRunning is true while running`() = testScope.runTest {
        // clear initial sync calls
        clearMocks(syncService, answers = false, recordedCalls = true, childMocks = false, exclusionRules = false)
        verify { syncService wasNot Called }

        val semaphore = Semaphore(1, 1)
        coEvery { syncService.syncTools(any()) } coAnswers {
            semaphore.acquire()
            true
        }

        viewModel.isSyncRunning.test {
            runCurrent()
            assertFalse(expectMostRecentItem())

            viewModel.triggerSync()
            runCurrent()
            assertTrue(expectMostRecentItem())

            semaphore.release()
            runCurrent()
            assertFalse(expectMostRecentItem())
        }

        coVerifyAll {
            syncService.syncFollowupsAsync()
            syncService.syncToolSharesAsync()
            syncService.syncTools(false)
            syncService.syncFavoriteTools(false)
        }
    }
}
