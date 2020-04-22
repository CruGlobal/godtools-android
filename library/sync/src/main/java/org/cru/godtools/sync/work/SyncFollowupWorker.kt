package org.cru.godtools.sync.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.ccci.gto.android.common.dagger.workmanager.AssistedWorkerFactory
import org.cru.godtools.sync.task.FollowupSyncTasks

private const val WORK_NAME = "SyncFollowup"

internal fun Context.scheduleSyncFollowupWork() = WorkManager.getInstance(this)
    .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, SyncWorkRequestBuilder<SyncFollowupWorker>().build())

class SyncFollowupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    @AssistedInject.Factory
    interface Factory : AssistedWorkerFactory

    override suspend fun doWork() =
        if (FollowupSyncTasks.getInstance(applicationContext).syncFollowups()) Result.success() else Result.retry()
}
