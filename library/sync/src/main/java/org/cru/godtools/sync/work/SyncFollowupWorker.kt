package org.cru.godtools.sync.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.dagger.workmanager.AssistedWorkerFactory
import org.cru.godtools.sync.task.FollowupSyncTasks

private const val WORK_NAME = "SyncFollowup"

internal fun WorkManager.scheduleSyncFollowupWork() =
    enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, SyncWorkRequestBuilder<SyncFollowupWorker>().build())

class SyncFollowupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val followupSyncTasks: FollowupSyncTasks
) : CoroutineWorker(context, workerParams) {
    @AssistedFactory
    interface Factory : AssistedWorkerFactory<SyncFollowupWorker>

    override suspend fun doWork() = if (followupSyncTasks.syncFollowups()) Result.success() else Result.retry()
}
