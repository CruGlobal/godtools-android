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

private const val WORK_NAME = "SyncTools"

private val SYNC_TOOLS_WORK_REQUEST = SyncWorkRequestBuilder<SyncToolsWorker>().build()

internal fun WorkManager.scheduleSyncToolsWork() =
    enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, SYNC_TOOLS_WORK_REQUEST)

@HiltWorker
internal class SyncToolsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val toolSyncTasks: ToolSyncTasks
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork() = if (toolSyncTasks.syncTools()) Result.success() else Result.retry()
}
