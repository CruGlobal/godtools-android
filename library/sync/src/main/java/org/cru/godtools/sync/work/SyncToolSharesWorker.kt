package org.cru.godtools.sync.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.cru.godtools.sync.task.ToolSyncTasks
import java.io.IOException

private const val WORK_NAME = "SyncToolShares"

internal fun Context.scheduleSyncToolSharesWork() = WorkManager.getInstance(this)
    .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, SyncWorkRequestBuilder<SyncToolSharesWorker>().build())

class SyncToolSharesWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork() = try {
        ToolSyncTasks.getInstance(applicationContext).syncSharesBlocking()
        Result.success()
    } catch (e: IOException) {
        Result.retry()
    }
}
