package org.cru.godtools.sync.task

import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.coVerifySequence
import io.mockk.just
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.api.ToolsApi
import org.cru.godtools.api.ViewsApi
import org.cru.godtools.db.repository.LastSyncTimeRepository
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
    private val lastSyncTimeRepository: LastSyncTimeRepository = mockk {
        coEvery { isLastSyncStale(*anyVararg(), staleAfter = any()) } returns true
        coEvery { updateLastSyncTime(*anyVararg()) } just Runs
    }

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
            lastSyncTimeRepository.isLastSyncStale(*anyVararg(), staleAfter = any())

            toolsApi.list(any())
            toolsRepository.getAllTools()

            syncRepository.storeTools(
                tools = listOf(tool),
                existingTools = existingTools.mapNotNullTo(mutableSetOf()) { it.code },
                includes = any()
            )

            lastSyncTimeRepository.updateLastSyncTime(*anyVararg())
        }
    }

    @Test
    fun `syncTools(force = false) - already synced`() = runTest {
        coEvery { lastSyncTimeRepository.isLastSyncStale(*anyVararg(), staleAfter = any()) } returns false

        tasks.syncTools(force = false)
        coVerifyAll {
            lastSyncTimeRepository.isLastSyncStale(*anyVararg(), staleAfter = any())

            toolsApi wasNot Called
            syncRepository wasNot Called
            viewsApi wasNot Called
        }
    }

    @Test
    fun `syncTools(force = true) - already synced`() = runTest {
        coEvery { lastSyncTimeRepository.isLastSyncStale(*anyVararg(), staleAfter = any()) } returns false

        tasks.syncTools(force = true)
        coVerifySequence {
            toolsApi.list(any())
            toolsRepository.getAllTools()

            syncRepository.storeTools(
                tools = listOf(tool),
                existingTools = existingTools.mapNotNullTo(mutableSetOf()) { it.code },
                includes = any()
            )

            lastSyncTimeRepository.updateLastSyncTime(*anyVararg())
        }
    }
    // endregion syncTools()
}
