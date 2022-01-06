package org.cru.godtools.sync.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.cru.godtools.sync.task.ToolSyncTasks

private const val WORK_NAME = "SyncToolShares"

internal fun WorkManager.scheduleSyncToolSharesWork() =
    enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, SyncWorkRequestBuilder<SyncToolSharesWorker>().build())

@HiltWorker
internal class SyncToolSharesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val toolSyncTasks: ToolSyncTasks
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork() = if (toolSyncTasks.syncShares()) Result.success() else Result.retry()
}
