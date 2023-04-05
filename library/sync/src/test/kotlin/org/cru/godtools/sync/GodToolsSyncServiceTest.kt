package org.cru.godtools.sync

import androidx.work.WorkManager
import io.mockk.Awaits
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.just
import io.mockk.mockk
import java.io.IOException
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.sync.task.ToolSyncTasks
import org.cru.godtools.sync.work.scheduleSyncToolsWork
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsSyncServiceTest {
    private val toolsSyncTasks = mockk<ToolSyncTasks> { coEvery { syncTools(any()) } returns true }

    private val timber: Timber.Tree = mockk(relaxed = true)
    private val workManager: WorkManager = mockk()
    private val testScope = TestScope()

    private val syncService = GodToolsSyncService(
        eventBus = mockk(relaxUnitFun = true),
        workManager = { workManager },
        syncTasks = mapOf(
            ToolSyncTasks::class.java to Provider { toolsSyncTasks },
        ),
        coroutineDispatcher = UnconfinedTestDispatcher(testScope.testScheduler),
        coroutineScope = testScope
    )

    @Before
    fun setup() {
        Timber.plant(timber)
        excludeRecords { Timber.tag(any()) }
    }

    @After
    fun cleanup() {
        Timber.uproot(timber)
    }

    // region syncTools()
    @Test
    fun `syncTools()`() = testScope.runTest {
        syncService.syncTools(false)
        coVerifyAll {
            toolsSyncTasks.syncTools(false)
            timber wasNot Called
            workManager wasNot Called
        }
    }

    @Test
    fun `syncTools() - Cancelled`() = testScope.runTest {
        coEvery { toolsSyncTasks.syncTools(any()) } just Awaits
        every { workManager.scheduleSyncToolsWork() } returns mockk()

        val job = async { syncService.syncTools(false) }
        runCurrent()
        job.cancelAndJoin()
        assertTrue(job.isCancelled)
        coVerifyAll {
            toolsSyncTasks.syncTools(false)
            timber wasNot Called
            workManager.scheduleSyncToolsWork()
        }
    }

    @Test
    fun `syncTools() - Failure`() = testScope.runTest {
        coEvery { toolsSyncTasks.syncTools(any()) } returns false
        every { workManager.scheduleSyncToolsWork() } returns mockk()

        syncService.syncTools(false)
        coVerifyAll {
            toolsSyncTasks.syncTools(false)
            timber wasNot Called
            workManager.scheduleSyncToolsWork()
        }
    }

    @Test
    fun `syncTools() - Failure - IOException`() = testScope.runTest {
        coEvery { toolsSyncTasks.syncTools(any()) } throws IOException()
        every { workManager.scheduleSyncToolsWork() } returns mockk()

        syncService.syncTools(false)
        coVerifyAll {
            toolsSyncTasks.syncTools(false)
            timber wasNot Called
            workManager.scheduleSyncToolsWork()
        }
    }

    @Test
    fun `syncTools() - Failure - Other Exception`() = testScope.runTest {
        val e = Exception()
        coEvery { toolsSyncTasks.syncTools(any()) } throws e
        every { workManager.scheduleSyncToolsWork() } returns mockk()

        syncService.syncTools(false)
        coVerifyAll {
            toolsSyncTasks.syncTools(false)
            timber.e(e, any())
            workManager.scheduleSyncToolsWork()
        }
    }
    // endregion syncTools()
}
