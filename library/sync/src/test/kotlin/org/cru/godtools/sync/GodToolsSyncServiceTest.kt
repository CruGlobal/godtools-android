package org.cru.godtools.sync

import androidx.work.WorkManager
import io.mockk.Awaits
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.io.IOException
import javax.inject.Provider
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.sync.task.ToolSyncTasks
import org.cru.godtools.sync.task.UserCounterSyncTasks
import org.cru.godtools.sync.work.scheduleSyncToolSharesWork
import org.cru.godtools.sync.work.scheduleSyncToolsWork
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsSyncServiceTest {
    private val toolsSyncTasks: ToolSyncTasks = mockk {
        coEvery { syncTools(any()) } returns true
        coEvery { syncShares() } returns true
    }
    private val userCounterSyncTasks: UserCounterSyncTasks = mockk {
        coEvery { syncCounters(any()) } returns true
        coEvery { syncDirtyCounters() } returns true
    }

    private data class LogEntry(val priority: Int, val tag: String?, val message: String, val t: Throwable?)
    private val timber = object : Timber.Tree() {
        val logs = mutableListOf<LogEntry>()
        fun assertNoLogs() = assertTrue(logs.isEmpty())

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            logs += LogEntry(priority, tag, message, t)
        }
    }
    private val workManager: WorkManager = mockk()
    private val testScope = TestScope()

    private val syncService = GodToolsSyncService(
        workManager = { workManager },
        syncTasks = mapOf(
            ToolSyncTasks::class.java to Provider { toolsSyncTasks },
            UserCounterSyncTasks::class.java to Provider { userCounterSyncTasks },
        ),
        coroutineDispatcher = UnconfinedTestDispatcher(testScope.testScheduler),
        coroutineScope = testScope.backgroundScope
    )

    @BeforeTest
    fun setup() {
        Timber.plant(timber)
    }

    @AfterTest
    fun cleanup() {
        Timber.uproot(timber)
    }

    // region syncTools()
    @Test
    fun `syncTools()`() = testScope.runTest {
        syncService.syncTools(false)
        coVerifyAll {
            toolsSyncTasks.syncTools(false)
            workManager wasNot Called
        }
        timber.assertNoLogs()
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
            workManager.scheduleSyncToolsWork()
        }
        timber.assertNoLogs()
    }

    @Test
    fun `syncTools() - Failure`() = testScope.runTest {
        coEvery { toolsSyncTasks.syncTools(any()) } returns false
        every { workManager.scheduleSyncToolsWork() } returns mockk()

        syncService.syncTools(false)
        coVerifyAll {
            toolsSyncTasks.syncTools(false)
            workManager.scheduleSyncToolsWork()
        }
        timber.assertNoLogs()
    }

    @Test
    fun `syncTools() - Failure - IOException`() = testScope.runTest {
        coEvery { toolsSyncTasks.syncTools(any()) } throws IOException()
        every { workManager.scheduleSyncToolsWork() } returns mockk()

        syncService.syncTools(false)
        coVerifyAll {
            toolsSyncTasks.syncTools(false)
            workManager.scheduleSyncToolsWork()
        }
        timber.assertNoLogs()
    }

    @Test
    fun `syncTools() - Failure - Other Exception`() = testScope.runTest {
        val e = Exception()
        coEvery { toolsSyncTasks.syncTools(any()) } throws e
        every { workManager.scheduleSyncToolsWork() } returns mockk()

        syncService.syncTools(false)
        coVerifyAll {
            toolsSyncTasks.syncTools(false)
            workManager.scheduleSyncToolsWork()
        }
        assertEquals(1, timber.logs.size)
        assertEquals(e, timber.logs[0].t)
    }
    // endregion syncTools()

    // region syncToolSharesAsync()
    @Test
    fun `syncToolSharesAsync()`() = testScope.runTest {
        syncService.syncToolSharesAsync().await()
        coVerifyAll {
            toolsSyncTasks.syncShares()
            workManager wasNot Called
        }
        timber.assertNoLogs()
    }

    @Test
    fun `syncToolSharesAsync() - Cancelled`() = testScope.runTest {
        coEvery { toolsSyncTasks.syncShares() } just Awaits
        every { workManager.scheduleSyncToolSharesWork() } returns mockk()

        val job = syncService.syncToolSharesAsync()
        runCurrent()
        job.cancelAndJoin()
        assertTrue(job.isCancelled)
        coVerifyAll {
            toolsSyncTasks.syncShares()
            workManager.scheduleSyncToolSharesWork()
        }
        timber.assertNoLogs()
    }

    @Test
    fun `syncToolSharesAsync() - Failure`() = testScope.runTest {
        coEvery { toolsSyncTasks.syncShares() } returns false
        every { workManager.scheduleSyncToolSharesWork() } returns mockk()

        syncService.syncToolSharesAsync().await()
        coVerifyAll {
            toolsSyncTasks.syncShares()
            workManager.scheduleSyncToolSharesWork()
        }
        timber.assertNoLogs()
    }
    // endregion syncToolSharesAsync()

    // region syncUserCounters()
    @Test
    fun `syncUserCounters(force = true)`() = testScope.runTest {
        assertTrue(syncService.syncUserCounters(force = true))
        runCurrent()
        coVerifyAll {
            userCounterSyncTasks.syncCounters(true)
            userCounterSyncTasks.syncDirtyCounters()
        }
    }

    @Test
    fun `syncUserCounters(force = false)`() = testScope.runTest {
        assertTrue(syncService.syncUserCounters(force = false))
        runCurrent()
        coVerifyAll {
            userCounterSyncTasks.syncCounters(false)
            userCounterSyncTasks.syncDirtyCounters()
        }
    }

    @Test
    fun `syncUserCounters() - Don't wait for dirty sync to finish`() = testScope.runTest {
        coEvery { userCounterSyncTasks.syncDirtyCounters() } just Awaits

        assertTrue(syncService.syncUserCounters())
        runCurrent()
        coVerifyAll {
            userCounterSyncTasks.syncCounters(any())
            userCounterSyncTasks.syncDirtyCounters()
        }
    }

    @Test
    fun `syncUserCounters() - Failure - syncCounters() returns false`() = testScope.runTest {
        coEvery { userCounterSyncTasks.syncCounters(any()) } returns false

        assertFalse(syncService.syncUserCounters())
        runCurrent()
        coVerifyAll {
            userCounterSyncTasks.syncCounters(any())
            userCounterSyncTasks.syncDirtyCounters()
        }
    }

    @Test
    fun `syncUserCounters() - Failure - syncCounters() throws Exception`() = testScope.runTest {
        coEvery { userCounterSyncTasks.syncCounters(any()) } throws Exception()

        assertFalse(syncService.syncUserCounters())
        runCurrent()
        coVerifyAll {
            userCounterSyncTasks.syncCounters(any())
            userCounterSyncTasks.syncDirtyCounters()
        }
    }

    @Test
    fun `syncUserCounters() - Jira - GT-1992`() = testScope.runTest {
        // syncDirtyCounters throwing an exception wasn't being handled within syncUserCounters()
        coEvery { userCounterSyncTasks.syncDirtyCounters() } throws Exception()

        assertTrue(syncService.syncUserCounters())
        runCurrent()
        coVerifyAll {
            userCounterSyncTasks.syncCounters(any())
            userCounterSyncTasks.syncDirtyCounters()
        }
    }
    // endregion syncUserCounters()
}
