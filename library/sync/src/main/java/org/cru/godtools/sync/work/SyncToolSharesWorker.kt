package org.cru.godtools.sync.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.cru.godtools.sync.task.ToolSyncTasks

private const val WORK_NAME = "SyncToolShares"

internal fun Context.scheduleSyncToolSharesWork() = WorkManager.getInstance(this)
    .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, SyncWorkRequestBuilder<SyncToolSharesWorker>().build())

class SyncToolSharesWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork() =
        if (ToolSyncTasks.getInstance(applicationContext).syncShares()) Result.success() else Result.retry()
}
