package org.cru.godtools.sync.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.cru.godtools.sync.task.FollowupSyncTasks

private const val WORK_NAME = "SyncFollowup"

internal fun Context.scheduleSyncFollowupWork() = WorkManager.getInstance(this)
    .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, SyncWorkRequestBuilder<SyncFollowupWorker>().build())

class SyncFollowupWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork() =
        if (FollowupSyncTasks.getInstance(applicationContext).syncFollowups()) Result.success() else Result.retry()
}
