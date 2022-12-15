package org.cru.godtools.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import java.io.IOException
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.cru.godtools.sync.task.BaseSyncTasks
import org.cru.godtools.sync.task.ToolSyncTasks
import org.cru.godtools.sync.work.scheduleSyncToolsWork
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsSyncServiceTest {
    private val toolsSyncTasks = mockk<ToolSyncTasks> { coEvery { syncTools(any()) } returns true }

    private val syncTasks = mapOf<Class<out BaseSyncTasks>, Provider<BaseSyncTasks>>(
        ToolSyncTasks::class.java to Provider { toolsSyncTasks }
    )
    private val timber: Timber.Tree = mockk(relaxed = true)
    private val workManager: WorkManager = mockk()
    private val testScope = TestScope()

    private val syncService =
        GodToolsSyncService(mockk(relaxUnitFun = true), { workManager }, syncTasks, UnconfinedTestDispatcher())

    @Before
    fun setup() {
        Timber.plant(timber)
        excludeRecords { Timber.tag(any()) }
    }

    @After
    fun cleanup() {
        Timber.uproot(timber)
    }

    @Test
    fun `syncTools()`() = testScope.runTest {
        syncService.syncTools(false)
        coVerifyAll {
            toolsSyncTasks.syncTools(false)
            workManager wasNot Called
        }
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
}
