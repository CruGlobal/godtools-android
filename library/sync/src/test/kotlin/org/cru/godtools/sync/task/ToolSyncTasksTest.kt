package org.cru.godtools.sync.task

import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.coVerifySequence
import io.mockk.just
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.api.ToolsApi
import org.cru.godtools.api.ViewsApi
import org.cru.godtools.db.repository.InMemoryLastSyncTimeRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.randomTool
import org.cru.godtools.sync.repository.SyncRepository
import retrofit2.Response

class ToolSyncTasksTest {
    private val tool = randomTool()
    private val existingTools = listOf(randomTool())

    private val toolsApi: ToolsApi = mockk {
        coEvery { list(any()) } returns Response.success(JsonApiObject.single(tool))
    }
    private val viewsApi: ViewsApi = mockk()
    private val syncRepository: SyncRepository = mockk {
        coEvery { storeTools(tools = any(), existingTools = any(), includes = any()) } just Runs
    }
    private val toolsRepository: ToolsRepository = mockk {
        coEvery { getAllTools() } returns existingTools
    }
    private val lastSyncTimeRepository = InMemoryLastSyncTimeRepository()

    private val tasks = ToolSyncTasks(
        toolsApi = toolsApi,
        viewsApi = viewsApi,
        syncRepository = syncRepository,
        toolsRepository = toolsRepository,
        lastSyncTimeRepository = lastSyncTimeRepository,
    )

    // region syncTools()
    @Test
    fun `syncTools()`() = runTest {
        tasks.syncTools()
        coVerifySequence {
            toolsApi.list(any())
            toolsRepository.getAllTools()

            syncRepository.storeTools(
                tools = listOf(tool),
                existingTools = existingTools.mapNotNullTo(mutableSetOf()) { it.code },
                includes = any()
            )
        }
        assertFalse(lastSyncTimeRepository.isLastSyncStale(ToolSyncTasks.SYNC_TIME_TOOLS, staleAfter = 60_000))
    }

    @Test
    fun `syncTools(force = false) - already synced`() = runTest {
        lastSyncTimeRepository.setLastSyncTime(ToolSyncTasks.SYNC_TIME_TOOLS, time = System.currentTimeMillis())

        tasks.syncTools(force = false)
        coVerifyAll {
            toolsApi wasNot Called
            syncRepository wasNot Called
            viewsApi wasNot Called
        }
    }

    @Test
    fun `syncTools(force = true) - already synced`() = runTest {
        lastSyncTimeRepository.setLastSyncTime(ToolSyncTasks.SYNC_TIME_TOOLS, time = System.currentTimeMillis())

        tasks.syncTools(force = true)
        coVerifySequence {
            toolsApi.list(any())
            toolsRepository.getAllTools()

            syncRepository.storeTools(
                tools = listOf(tool),
                existingTools = existingTools.mapNotNullTo(mutableSetOf()) { it.code },
                includes = any()
            )
        }
    }
    // endregion syncTools()
}
