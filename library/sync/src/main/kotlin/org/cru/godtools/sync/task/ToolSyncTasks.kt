package org.cru.godtools.sync.task

import androidx.annotation.AnyThread
import java.io.IOException
import java.net.HttpURLConnection.HTTP_OK
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.sync.repository.SyncRepository

private const val SYNC_TIME_TOOL = "last_synced.tool."
private const val STALE_DURATION_TOOLS = TimeConstants.DAY_IN_MS

@Singleton
internal class ToolSyncTasks @Inject internal constructor(
    private val toolsApi: ToolsApi,
    private val viewsApi: ViewsApi,
    private val syncRepository: SyncRepository,
    private val toolsRepository: ToolsRepository,
    private val lastSyncTimeRepository: LastSyncTimeRepository,
) : BaseSyncTasks() {
    internal companion object {
        const val SYNC_TIME_TOOLS = "last_synced.tools"
        const val SYNC_TIME_FEATURED_TOOLS = "last_synced.featured_tools."
        const val SYNC_TIME_TOOL_ORDER = "last_synced.tool_order."

        private val INCLUDES_GET_TOOL = Includes(
            Tool.JSON_ATTACHMENTS,
            "${Tool.JSON_METATOOL}.${Tool.JSON_DEFAULT_VARIANT}",
            "${Tool.JSON_LATEST_TRANSLATIONS}.${Translation.JSON_LANGUAGE}",
        )

        private fun buildApiParams() = JsonApiParams()
            .includes(INCLUDES_GET_TOOL)
            .fields(Tool.JSONAPI_TYPE, *Tool.JSONAPI_FIELDS)
            .fields(Language.JSONAPI_TYPE, *Language.JSONAPI_FIELDS)

        private fun buildToolPlaceholderParams() = JsonApiParams()
            .fields(Tool.JSONAPI_TYPE, Tool.JSON_CODE)
    }

    private val toolsMutex = Mutex()
    private val toolMutex = MutexMap()
    private val sharesMutex = Mutex()

    @AnyThread
    internal suspend fun syncTools(force: Boolean = false) = toolsMutex.withLock {
        // short-circuit if we aren't forcing a sync and the data isn't stale
        if (!force && !lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_TOOLS, staleAfter = STALE_DURATION_TOOLS)) {
            return@withLock true
        }

        // fetch tools from the API, short-circuit if this response is invalid
        val json = toolsApi.list(buildApiParams()).takeIf { it.code() == HTTP_OK }?.body() ?: return@withLock false

        // store fetched tools
        syncRepository.storeTools(
            tools = json.data,
            existingTools = toolsRepository.getAllTools().mapNotNullTo(mutableSetOf()) { it.code },
            includes = INCLUDES_GET_TOOL
        )
        lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_TOOLS)
        true
    }

    internal suspend fun syncTool(toolCode: String, force: Boolean = false) = toolMutex.withLock(toolCode) {
        // short-circuit if we aren't forcing a sync and the data isn't stale
        if (!force &&
            !lastSyncTimeRepository.isLastSyncStale(SYNC_TIME_TOOL, toolCode, staleAfter = STALE_DURATION_TOOLS)
        ) {
            return@withLock true
        }

        // fetch tools from the API, short-circuit if this response is invalid
        val json = toolsApi.getTool(toolCode, buildApiParams()).takeIf { it.code() == HTTP_OK }?.body() ?: return false

        // store fetched tools
        syncRepository.storeTools(
            json.data,
            existingTools = mutableSetOf(toolCode),
            includes = INCLUDES_GET_TOOL
        )
        lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_TOOL, toolCode)

        true
    }

    // region Featured Tools
    private val featuredToolsMutex = MutexMap()

    internal suspend fun syncFeaturedTools(locale: Locale, country: String, force: Boolean = false): Boolean {
        val normalizedCountry = country.uppercase()

        featuredToolsMutex.withLock(locale to normalizedCountry) {
            if (!force &&
                !lastSyncTimeRepository.isLastSyncStale(
                    SYNC_TIME_FEATURED_TOOLS,
                    locale,
                    normalizedCountry,
                    staleAfter = STALE_DURATION_TOOLS,
                )
            ) {
                return true
            }

            val tools = toolsApi.getFeaturedTools(locale, normalizedCountry, buildToolPlaceholderParams())
                .takeIf { it.code() == HTTP_OK }?.body()?.data ?: return false

            toolsRepository.storeFeaturedToolsFromSync(locale, normalizedCountry, tools)
            lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_FEATURED_TOOLS, locale, normalizedCountry)

            return true
        }
    }
    // endregion Featured Tools

    // region Tool Order
    private val toolOrderMutex = MutexMap()

    internal suspend fun syncToolOrder(locale: Locale, country: String?, force: Boolean = false): Boolean {
        val normalizedCountry = country?.uppercase()

        toolOrderMutex.withLock(locale to normalizedCountry) {
            if (!force &&
                !lastSyncTimeRepository.isLastSyncStale(
                    SYNC_TIME_TOOL_ORDER,
                    locale,
                    normalizedCountry.orEmpty(),
                    staleAfter = STALE_DURATION_TOOLS,
                )
            ) {
                return true
            }

            val tools = toolsApi.getToolOrder(locale, normalizedCountry, buildToolPlaceholderParams())
                .takeIf { it.code() == HTTP_OK }?.body()?.data ?: return false

            toolsRepository.storePersonalizedToolOrderFromSync(locale, normalizedCountry, tools)
            lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_TOOL_ORDER, locale, normalizedCountry.orEmpty())

            return true
        }
    }
    // endregion Tool Order

    /**
     * @return true if all pending share counts were successfully synced. false if any failed to sync.
     */
    internal suspend fun syncShares() = coroutineScope {
        sharesMutex.withLock {
            toolsRepository.getAllTools()
                .filter { it.pendingShares > 0 }
                .map { tool ->
                    async {
                        val code = tool.code ?: return@async true
                        val views = ToolViews(tool)
                        try {
                            viewsApi.submitViews(views).isSuccessful
                                .also { if (it) toolsRepository.updateToolViews(code, 0 - views.quantity) }
                        } catch (_: IOException) {
                            false
                        }
                    }
                }.awaitAll().all { it }
        }
    }
}
