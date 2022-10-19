package org.cru.godtools.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.cru.godtools.sync.task.BaseSyncTasks
import org.cru.godtools.sync.task.ToolSyncTasks
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsSyncServiceTest {
    private val toolsSyncTasks = mockk<ToolSyncTasks> { coEvery { syncTools(any()) } returns true }

    private val syncTasks = mapOf<Class<out BaseSyncTasks>, Provider<BaseSyncTasks>>(
        ToolSyncTasks::class.java to Provider { toolsSyncTasks }
    )
    private val workManager: WorkManager = mockk()
    private val testScope = TestScope()

    private val syncService =
        GodToolsSyncService(mockk(relaxUnitFun = true), { workManager }, syncTasks, UnconfinedTestDispatcher())

    @Test
    fun testSyncTools() = testScope.runTest {
        syncService.syncTools(false)
        coVerify { toolsSyncTasks.syncTools(any()) }
    }
}
