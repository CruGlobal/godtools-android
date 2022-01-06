package org.cru.godtools.sync.task

import android.os.Bundle
import androidx.annotation.AnyThread
import androidx.collection.SimpleArrayMap
import java.io.IOException
import java.net.HttpURLConnection.HTTP_OK
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.base.TimeConstants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.ccci.gto.android.common.jsonapi.util.Includes
import org.cru.godtools.api.ToolsApi
import org.cru.godtools.api.ViewsApi
import org.cru.godtools.api.model.ToolViews
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

private const val SYNC_TIME_TOOLS = "last_synced.tools"
private const val STALE_DURATION_TOOLS = TimeConstants.DAY_IN_MS

private val API_GET_INCLUDES = arrayOf(
    Tool.JSON_ATTACHMENTS,
    "${Tool.JSON_LATEST_TRANSLATIONS}.${Translation.JSON_LANGUAGE}"
)

@Singleton
class ToolSyncTasks @Inject internal constructor(
    dao: GodToolsDao,
    private val toolsApi: ToolsApi,
    private val viewsApi: ViewsApi,
    eventBus: EventBus
) : BaseDataSyncTasks(dao, eventBus) {
    private val toolsMutex = Mutex()
    private val sharesMutex = Mutex()

    @AnyThread
    suspend fun syncTools(args: Bundle = Bundle.EMPTY) = withContext(Dispatchers.IO) {
        toolsMutex.withLock {
            // short-circuit if we aren't forcing a sync and the data isn't stale
            if (!isForced(args) &&
                System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_TOOLS) < STALE_DURATION_TOOLS
            ) return@withContext true

            // fetch tools from the API, short-circuit if this response is invalid
            val json = toolsApi.list(JsonApiParams().include(*API_GET_INCLUDES))
                .takeIf { it.code() == HTTP_OK }?.body() ?: return@withContext false

            // store fetched tools
            val events = SimpleArrayMap<Class<*>, Any>()
            dao.transaction {
                val existing = index(Query.select<Tool>().get(dao))
                storeTools(events, json.data, existing, Includes(*API_GET_INCLUDES))
            }

            // send any pending events
            sendEvents(events)

            // update the sync time
            dao.updateLastSyncTime(SYNC_TIME_TOOLS)
            true
        }
    }

    /**
     * @return true if all pending share counts were successfully synced. false if any failed to sync.
     */
    suspend fun syncShares() = withContext(Dispatchers.IO) {
        sharesMutex.withLock {
            coroutineScope {
                Query.select<Tool>().where(ToolTable.SQL_WHERE_HAS_PENDING_SHARES).get(dao)
                    .map {
                        async {
                            try {
                                val views = ToolViews(it)
                                viewsApi.submitViews(views).isSuccessful
                                    .also { if (it) dao.updateSharesDelta(views.toolCode, 0 - views.quantity) }
                            } catch (ignored: IOException) {
                                false
                            }
                        }
                    }.all { it.await() }
            }
        }
    }
}
