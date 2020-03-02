package org.cru.godtools.sync.task

import android.content.Context
import android.os.Bundle
import androidx.annotation.RestrictTo
import org.ccci.gto.android.common.base.TimeConstants
import org.cru.godtools.base.util.SingletonHolder

private val LOCK_SYNC_GLOBAL_ANALYTICS = Any()
private const val SYNC_TIME_GLOBAL_ACTIVITY = "last_synced.global_activity"
private const val STALE_DURATION_GLOBAL_ACTIVITY = TimeConstants.DAY_IN_MS

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class GlobalActivitySyncTasks private constructor(context: Context) : BaseSyncTasks(context) {
    companion object : SingletonHolder<GlobalActivitySyncTasks, Context>(::GlobalActivitySyncTasks)

    fun syncGlobalActivity(args: Bundle): Boolean {
        synchronized(LOCK_SYNC_GLOBAL_ANALYTICS) {
            // short-circuit if we aren't forcing a sync and the data isn't stale
            if (!isForced(args) && System.currentTimeMillis() -
                dao.getLastSyncTime(SYNC_TIME_GLOBAL_ACTIVITY) < STALE_DURATION_GLOBAL_ACTIVITY
            ) return true

            val response = api.analytics.getGlobalActivity().execute()
            if (!response.isSuccessful) return false

            dao.transaction { response.body()?.data?.forEach { dao.replace(it) } }
            dao.updateLastSyncTime(SYNC_TIME_GLOBAL_ACTIVITY)
        }
        return true
    }
}
