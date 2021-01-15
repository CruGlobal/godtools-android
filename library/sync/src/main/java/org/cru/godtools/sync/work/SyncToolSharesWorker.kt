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
import org.cru.godtools.sync.task.ToolSyncTasks

private const val WORK_NAME = "SyncToolShares"

internal fun WorkManager.scheduleSyncToolSharesWork() =
    enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, SyncWorkRequestBuilder<SyncToolSharesWorker>().build())

class SyncToolSharesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val toolSyncTasks: ToolSyncTasks
) : CoroutineWorker(context, workerParams) {
    @AssistedFactory
    interface Factory : AssistedWorkerFactory<SyncToolSharesWorker>

    override suspend fun doWork() = if (toolSyncTasks.syncShares()) Result.success() else Result.retry()
}
