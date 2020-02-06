package org.cru.godtools.sync.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.cru.godtools.sync.task.FollowupSyncTasks
import java.io.IOException

private const val WORK_NAME = "SyncFollowup"

internal fun Context.scheduleSyncFollowupWork() {
    WorkManager.getInstance(this).enqueueUniqueWork(
        WORK_NAME, ExistingWorkPolicy.REPLACE,
        OneTimeWorkRequestBuilder<SyncFollowupWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(TAG_SYNC)
            .build()
    )
}

class SyncFollowupWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork() = try {
        FollowupSyncTasks(applicationContext).syncFollowups()
        Result.success()
    } catch (e: IOException) {
        Result.retry()
    }
}
