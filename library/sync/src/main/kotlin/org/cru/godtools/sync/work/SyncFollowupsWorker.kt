package org.cru.godtools.sync.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.cru.godtools.sync.task.FollowupSyncTasks

private const val WORK_NAME = "SyncFollowup"

internal fun WorkManager.scheduleSyncFollowupsWork() =
    enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, SyncWorkRequestBuilder<SyncFollowupsWorker>().build())

@HiltWorker
internal class SyncFollowupsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val followupSyncTasks: FollowupSyncTasks
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork() = if (followupSyncTasks.syncFollowups()) Result.success() else Result.retry()
}
