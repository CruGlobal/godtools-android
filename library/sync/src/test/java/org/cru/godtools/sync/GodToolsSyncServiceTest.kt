package org.cru.godtools.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import javax.inject.Provider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.cru.godtools.sync.task.BaseSyncTasks
import org.cru.godtools.sync.task.ToolSyncTasks
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsSyncServiceTest {
    private lateinit var eventBus: EventBus
    private lateinit var workManager: WorkManager
    private val syncTasks = mutableMapOf<Class<out BaseSyncTasks>, Provider<BaseSyncTasks>>()
    private val coroutineScope = TestCoroutineScope()

    private lateinit var syncService: GodToolsSyncService

    @Before
    fun setup() {
        eventBus = mock()
        workManager = mock()

        syncService = GodToolsSyncService(eventBus, workManager, syncTasks, coroutineScope)
    }

    @Test
    fun testSyncTools() {
        val toolsSyncTasks = mock<ToolSyncTasks>()
        syncTasks[ToolSyncTasks::class.java] = Provider { toolsSyncTasks }

        syncService.syncTools(false).sync()
        coroutineScope.advanceUntilIdle()
        verifyBlocking(toolsSyncTasks) { syncTools(any()) }
    }
}
