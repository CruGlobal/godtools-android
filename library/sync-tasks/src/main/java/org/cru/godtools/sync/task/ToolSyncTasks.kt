package org.cru.godtools.sync.task

import android.content.Context
import android.os.Bundle
import androidx.collection.SimpleArrayMap
import org.ccci.gto.android.common.base.TimeConstants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams
import org.ccci.gto.android.common.jsonapi.util.Includes
import org.cru.godtools.api.model.ToolViews
import org.cru.godtools.base.util.SingletonHolder
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.Contract.ToolTable
import java.io.IOException

private const val SYNC_TIME_TOOLS = "last_synced.tools"
private const val STALE_DURATION_TOOLS = TimeConstants.DAY_IN_MS
private val LOCK_SYNC_TOOLS = Any()
private val LOCK_SYNC_SHARES = Any()

private val API_GET_INCLUDES = arrayOf(
    Tool.JSON_ATTACHMENTS,
    "${Tool.JSON_LATEST_TRANSLATIONS}.${Translation.JSON_LANGUAGE}"
)

class ToolSyncTasks private constructor(context: Context) : BaseDataSyncTasks(context) {
    companion object : SingletonHolder<ToolSyncTasks, Context>(::ToolSyncTasks)

    @Throws(IOException::class)
    fun syncTools(args: Bundle): Boolean {
        val events = SimpleArrayMap<Class<*>, Any>()
        synchronized(LOCK_SYNC_TOOLS) {
            // short-circuit if we aren't forcing a sync and the data isn't stale
            if (!isForced(args) &&
                System.currentTimeMillis() - dao.getLastSyncTime(SYNC_TIME_TOOLS) < STALE_DURATION_TOOLS
            ) return true

            // fetch tools from the API, short-circuit if this response is invalid
            val json = api.tools.list(JsonApiParams().include(*API_GET_INCLUDES)).execute()
                .takeIf { it.code() == 200 }?.body() ?: return false

            // store fetched tools
            dao.transaction {
                val existing = index(Query.select<Tool>().get(dao))
                storeTools(events, json.data, existing, Includes(*API_GET_INCLUDES))
            }

            // send any pending events
            sendEvents(events)

            // update the sync time
            dao.updateLastSyncTime(SYNC_TIME_TOOLS)
        }
        return true
    }

    /**
     * @return true if all pending share counts were successfully synced. false if any failed to sync.
     */
    fun syncShares(): Boolean {
        var successful = true
        synchronized(LOCK_SYNC_SHARES) {
            Query.select<Tool>().where(ToolTable.SQL_WHERE_HAS_PENDING_SHARES).get(dao).forEach {
                val views = ToolViews(it)
                try {
                    if (api.views.submitViews(views).execute().isSuccessful) {
                        dao.updateSharesDelta(views.toolCode, 0 - views.quantity)
                    } else {
                        successful = false
                    }
                } catch (ignored: IOException) {
                    successful = false
                }
            }
        }
        return successful
    }
}
