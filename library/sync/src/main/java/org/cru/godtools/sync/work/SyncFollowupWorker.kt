package org.cru.godtools.sync.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.cru.godtools.sync.task.FollowupSyncTasks
import java.io.IOException

private const val WORK_NAME = "SyncFollowup"

internal fun Context.scheduleSyncFollowupWork() = WorkManager.getInstance(this)
    .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, SyncWorkRequestBuilder<SyncFollowupWorker>().build())

class SyncFollowupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork() = try {
        FollowupSyncTasks.getInstance(applicationContext).syncFollowupsBlocking()
        Result.success()
    } catch (e: IOException) {
        Result.retry()
    }
}
