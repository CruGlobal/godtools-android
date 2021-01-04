package org.cru.godtools.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verifyBlocking
import javax.inject.Provider
import org.cru.godtools.sync.task.BaseSyncTasks
import org.cru.godtools.sync.task.ToolSyncTasks
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GodToolsSyncServiceTest {
    private lateinit var eventBus: EventBus
    private lateinit var workManager: WorkManager
    private val syncTasks = mutableMapOf<Class<out BaseSyncTasks>, Provider<BaseSyncTasks>>()

    private lateinit var syncService: GodToolsSyncService

    @Before
    fun setup() {
        eventBus = mock()
        workManager = mock()

        syncService = GodToolsSyncService(eventBus, workManager, syncTasks)
    }

    @Test
    fun testSyncTools() {
        val toolsSyncTasks = mock<ToolSyncTasks>()
        syncTasks[ToolSyncTasks::class.java] = Provider { toolsSyncTasks }

        syncService.syncTools(false).sync()
        verifyBlocking(toolsSyncTasks) { syncTools(any()) }
    }
}
