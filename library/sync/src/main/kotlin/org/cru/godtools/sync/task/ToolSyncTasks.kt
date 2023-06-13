package org.cru.godtools.sync.task

import androidx.annotation.AnyThread
import java.io.IOException
import java.net.HttpURLConnection.HTTP_OK
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.base.TimeConstants
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.ccci.gto.android.common.jsonapi.util.Includes
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.cru.godtools.api.ToolsApi
import org.cru.godtools.api.ViewsApi
import org.cru.godtools.api.model.ToolViews
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.sync.repository.SyncRepository

private const val SYNC_TIME_TOOLS = "last_synced.tools"
private const val SYNC_TIME_TOOL = "last_synced.tool."
private const val STALE_DURATION_TOOLS = TimeConstants.DAY_IN_MS

private val API_GET_INCLUDES = arrayOf(
    Tool.JSON_ATTACHMENTS,
    "${Tool.JSON_METATOOL}.${Tool.JSON_DEFAULT_VARIANT}",
    "${Tool.JSON_LATEST_TRANSLATIONS}.${Translation.JSON_LANGUAGE}",
)

@Singleton
internal class ToolSyncTasks @Inject internal constructor(
    private val toolsApi: ToolsApi,
    private val viewsApi: ViewsApi,
    private val syncRepository: SyncRepository,
    private val toolsRepository: ToolsRepository,
    private val lastSyncTimeRepository: LastSyncTimeRepository,
) : BaseSyncTasks() {
    private val toolsMutex = Mutex()
    private val toolMutex = MutexMap()
    private val sharesMutex = Mutex()

    @AnyThread
    suspend fun syncTools(force: Boolean = false) = withContext(Dispatchers.IO) {
        toolsMutex.withLock {
            // short-circuit if we aren't forcing a sync and the data isn't stale
            if (!force && !lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_TOOLS, staleAfter = STALE_DURATION_TOOLS)) {
                return@withContext true
            }

            // fetch tools from the API, short-circuit if this response is invalid
            val json = toolsApi.list(JsonApiParams().include(*API_GET_INCLUDES))
                .takeIf { it.code() == HTTP_OK }?.body() ?: return@withContext false

            // store fetched tools
            syncRepository.storeTools(
                json.data,
                existingTools = toolsRepository.getResourcesBlocking().mapNotNull { it.code }.toMutableSet(),
                includes = Includes(*API_GET_INCLUDES)
            )
            lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_TOOLS)
            true
        }
    }

    internal suspend fun syncTool(toolCode: String, force: Boolean = false) = toolMutex.withLock(toolCode) {
        // short-circuit if we aren't forcing a sync and the data isn't stale
        if (!force &&
            !lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_TOOL, toolCode, staleAfter = STALE_DURATION_TOOLS)
        ) {
            return true
        }

        // fetch tools from the API, short-circuit if this response is invalid
        val json = toolsApi.getTool(toolCode, JsonApiParams().include(*API_GET_INCLUDES))
            .takeIf { it.code() == HTTP_OK }?.body() ?: return false

        // store fetched tools
        syncRepository.storeTools(
            json.data,
            existingTools = mutableSetOf(toolCode),
            includes = Includes(*API_GET_INCLUDES)
        )
        lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_TOOL, toolCode)

        true
    }

    /**
     * @return true if all pending share counts were successfully synced. false if any failed to sync.
     */
    suspend fun syncShares() = withContext(Dispatchers.IO) {
        sharesMutex.withLock {
            toolsRepository.getResources()
                .filter { it.pendingShares > 0 }
                .map { tool ->
                    async {
                        val code = tool.code ?: return@async true
                        val views = ToolViews(tool)
                        try {
                            viewsApi.submitViews(views).isSuccessful
                                .also { if (it) toolsRepository.updateToolViews(code, 0 - views.quantity) }
                        } catch (ignored: IOException) {
                            false
                        }
                    }
                }.awaitAll().all { it }
        }
    }
}
