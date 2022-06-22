package org.cru.godtools.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Test
    fun testSyncTools() = runTest(UnconfinedTestDispatcher()) {
        val syncService =
            GodToolsSyncService(mockk(relaxUnitFun = true), mockk(), syncTasks, UnconfinedTestDispatcher())
        syncService.syncTools(false).sync()
        coVerify { toolsSyncTasks.syncTools(any()) }
    }
}
