package org.cru.godtools.sync.task

import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.coVerifySequence
import io.mockk.just
import io.mockk.mockk
import java.util.Locale
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
import org.cru.godtools.sync.task.ToolSyncTasks.Companion.SYNC_TIME_DEFAULT_TOOL_ORDER
import org.cru.godtools.sync.task.ToolSyncTasks.Companion.SYNC_TIME_FEATURED_TOOLS
import org.cru.godtools.sync.task.ToolSyncTasks.Companion.SYNC_TIME_RANKED_TOOLS
import retrofit2.Response

class ToolSyncTasksTest {
    private val locale = Locale.ENGLISH
    private val country = "US"

    private val tool = randomTool()
    private val existingTools = listOf(randomTool())
    private val apiFeaturedTools = listOf(randomTool(), randomTool())
    private val apiDefaultToolOrder = listOf(randomTool(), randomTool())
    private val apiRankedTools = listOf(randomTool(), randomTool())

    private val toolsApi: ToolsApi = mockk {
        coEvery { list(any()) } returns Response.success(JsonApiObject.single(tool))
        coEvery { getFeaturedTools(any(), any(), any()) } returns
            Response.success(JsonApiObject.of(*apiFeaturedTools.toTypedArray()))
        coEvery { getDefaultToolOrder(any(), any()) } returns
            Response.success(JsonApiObject.of(*apiDefaultToolOrder.toTypedArray()))
        coEvery { getRankedTools(any(), any(), any()) } returns
            Response.success(JsonApiObject.of(*apiRankedTools.toTypedArray()))
    }
    private val viewsApi: ViewsApi = mockk()
    private val syncRepository: SyncRepository = mockk {
        coEvery { storeTools(tools = any(), existingTools = any(), includes = any()) } just Runs
    }
    private val toolsRepository: ToolsRepository = mockk {
        coEvery { getAllTools() } returns existingTools
        coEvery { storeFeaturedToolsFromSync(any(), any(), any()) } just Runs
        coEvery { storePersonalizedToolOrderFromSync(any(), any(), any()) } just Runs
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

    // region syncFeaturedTools()
    @Test
    fun `syncFeaturedTools()`() = runTest {
        tasks.syncFeaturedTools(locale, country)
        coVerifyAll {
            toolsApi.getFeaturedTools(locale, country, any())
            toolsRepository.storeFeaturedToolsFromSync(locale, country, apiFeaturedTools)
        }
        assertFalse(
            lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_FEATURED_TOOLS, locale, country, staleAfter = 60_000)
        )
    }

    @Test
    fun `syncFeaturedTools(force = false) - already synced`() = runTest {
        with(lastSyncTimeRepository) {
            setLastSyncTime(SYNC_TIME_FEATURED_TOOLS, locale, country, time = System.currentTimeMillis())
        }

        tasks.syncFeaturedTools(locale, country, force = false)
        coVerifyAll {
            toolsApi wasNot Called
            toolsRepository wasNot Called
        }
    }

    @Test
    fun `syncFeaturedTools(force = true) - already synced`() = runTest {
        with(lastSyncTimeRepository) {
            setLastSyncTime(SYNC_TIME_FEATURED_TOOLS, locale, country, time = System.currentTimeMillis())
        }

        tasks.syncFeaturedTools(locale, country, force = true)
        coVerifyAll {
            toolsApi.getFeaturedTools(locale, country, any())
            toolsRepository.storeFeaturedToolsFromSync(locale, country, apiFeaturedTools)
        }
        assertFalse(
            lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_FEATURED_TOOLS, locale, country, staleAfter = 60_000)
        )
    }
    // endregion syncFeaturedTools()

    // region syncDefaultToolOrder()
    @Test
    fun `syncDefaultToolOrder()`() = runTest {
        tasks.syncDefaultToolOrder(locale)
        coVerifyAll {
            toolsApi.getDefaultToolOrder(locale, any())
            toolsRepository.storePersonalizedToolOrderFromSync(locale, null, apiDefaultToolOrder)
        }
        assertFalse(
            lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_DEFAULT_TOOL_ORDER, locale, staleAfter = 60_000)
        )
    }

    @Test
    fun `syncDefaultToolOrder(force = false) - already synced`() = runTest {
        with(lastSyncTimeRepository) {
            setLastSyncTime(SYNC_TIME_DEFAULT_TOOL_ORDER, locale, time = System.currentTimeMillis())
        }

        tasks.syncDefaultToolOrder(locale, force = false)
        coVerifyAll {
            toolsApi wasNot Called
            toolsRepository wasNot Called
        }
    }

    @Test
    fun `syncDefaultToolOrder(force = true) - already synced`() = runTest {
        with(lastSyncTimeRepository) {
            setLastSyncTime(SYNC_TIME_DEFAULT_TOOL_ORDER, locale, time = System.currentTimeMillis())
        }

        tasks.syncDefaultToolOrder(locale, force = true)
        coVerifyAll {
            toolsApi.getDefaultToolOrder(locale, any())
            toolsRepository.storePersonalizedToolOrderFromSync(locale, null, apiDefaultToolOrder)
        }
        assertFalse(
            lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_DEFAULT_TOOL_ORDER, locale, staleAfter = 60_000)
        )
    }
    // endregion syncDefaultToolOrder()

    // region syncRankedTools()
    @Test
    fun `syncRankedTools()`() = runTest {
        tasks.syncRankedTools(locale, country)
        coVerifyAll {
            toolsApi.getRankedTools(locale, country, any())
            toolsRepository.storePersonalizedToolOrderFromSync(locale, country, apiRankedTools)
        }
        assertFalse(
            lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_RANKED_TOOLS, locale, country, staleAfter = 60_000)
        )
    }

    @Test
    fun `syncRankedTools(force = false) - already synced`() = runTest {
        with(lastSyncTimeRepository) {
            setLastSyncTime(SYNC_TIME_RANKED_TOOLS, locale, country, time = System.currentTimeMillis())
        }

        tasks.syncRankedTools(locale, country, force = false)
        coVerifyAll {
            toolsApi wasNot Called
            toolsRepository wasNot Called
        }
    }

    @Test
    fun `syncRankedTools(force = true) - already synced`() = runTest {
        with(lastSyncTimeRepository) {
            setLastSyncTime(SYNC_TIME_RANKED_TOOLS, locale, country, time = System.currentTimeMillis())
        }

        tasks.syncRankedTools(locale, country, force = true)
        coVerifyAll {
            toolsApi.getRankedTools(locale, country, any())
            toolsRepository.storePersonalizedToolOrderFromSync(locale, country, apiRankedTools)
        }
        assertFalse(
            lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_RANKED_TOOLS, locale, country, staleAfter = 60_000)
        )
    }
    // endregion syncRankedTools()
}
