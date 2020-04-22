package org.cru.godtools.sync.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.ccci.gto.android.common.dagger.workmanager.AssistedWorkerFactory
import org.cru.godtools.sync.task.ToolSyncTasks

private const val WORK_NAME = "SyncToolShares"

internal fun Context.scheduleSyncToolSharesWork() = WorkManager.getInstance(this)
    .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, SyncWorkRequestBuilder<SyncToolSharesWorker>().build())

class SyncToolSharesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    @AssistedInject.Factory
    interface Factory : AssistedWorkerFactory

    override suspend fun doWork() =
        if (ToolSyncTasks.getInstance(applicationContext).syncShares()) Result.success() else Result.retry()
}
