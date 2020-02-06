package org.cru.godtools.sync.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.cru.godtools.sync.task.ToolSyncTasks
import java.io.IOException

private const val WORK_NAME = "SyncFollowup"

internal fun Context.scheduleSyncToolSharesWork() {
    WorkManager.getInstance(this).enqueueUniqueWork(
        WORK_NAME, ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<SyncToolSharesWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(TAG_SYNC)
            .build()
    )
}

class SyncToolSharesWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork() = try {
        ToolSyncTasks(applicationContext).syncShares()
        Result.success()
    } catch (e: IOException) {
        Result.retry()
    }
}
