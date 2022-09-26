package org.cru.godtools.sync.task

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ccci.gto.android.common.base.TimeConstants.DAY_IN_MS
import org.cru.godtools.api.AnalyticsApi
import org.cru.godtools.db.repository.GlobalActivityRepository
import org.cru.godtools.db.repository.LastSyncTimeRepository

private const val SYNC_TIME_GLOBAL_ACTIVITY = "last_synced.global_activity"
private const val STALE_DURATION_GLOBAL_ACTIVITY = DAY_IN_MS

@Singleton
internal class AnalyticsSyncTasks @Inject internal constructor(
    private val analyticsApi: AnalyticsApi,
    private val globalActivityRepository: GlobalActivityRepository,
    private val lastSyncTimeRepository: LastSyncTimeRepository
) : BaseSyncTasks() {
    private val globalActivityMutex = Mutex()

    suspend fun syncGlobalActivity(args: Bundle) = globalActivityMutex.withLock {
        // short-circuit if we aren't forcing a sync and the data isn't stale
        if (!isForced(args) && !lastSyncTimeRepository
            .isLastSyncStale(SYNC_TIME_GLOBAL_ACTIVITY, staleAfter = STALE_DURATION_GLOBAL_ACTIVITY)
        ) return@withLock

        analyticsApi.getGlobalActivity().takeIf { it.isSuccessful }?.body()?.let {
            globalActivityRepository.updateGlobalActivity(it)
            lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_GLOBAL_ACTIVITY)
        }
    }
}
