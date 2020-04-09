package org.cru.godtools.sync.task

import android.content.Context
import android.os.Bundle
import androidx.annotation.RestrictTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.base.TimeConstants
import javax.inject.Inject
import javax.inject.Singleton

private const val SYNC_TIME_GLOBAL_ACTIVITY = "last_synced.global_activity"
private const val STALE_DURATION_GLOBAL_ACTIVITY = TimeConstants.DAY_IN_MS

@Singleton
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AnalyticsSyncTasks @Inject internal constructor(context: Context) : BaseSyncTasks(context) {
    private val globalActivityMutex = Mutex()

    suspend fun syncGlobalActivity(args: Bundle) = withContext(Dispatchers.IO) {
        globalActivityMutex.withLock {
            // short-circuit if we aren't forcing a sync and the data isn't stale
            if (!isForced(args) && System.currentTimeMillis() -
                dao.getLastSyncTime(SYNC_TIME_GLOBAL_ACTIVITY) < STALE_DURATION_GLOBAL_ACTIVITY
            ) return@withContext

            api.analytics.getGlobalActivity().takeIf { it.isSuccessful }?.body()?.let {
                dao.replace(it)
                dao.updateLastSyncTime(SYNC_TIME_GLOBAL_ACTIVITY)
            }
        }
    }
}
