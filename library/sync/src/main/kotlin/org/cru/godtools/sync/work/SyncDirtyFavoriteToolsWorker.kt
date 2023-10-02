package org.cru.godtools.sync.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.cru.godtools.sync.task.UserFavoriteToolsSyncTasks

private const val WORK_NAME = "SyncDirtyFavoriteTools"

internal fun WorkManager.scheduleSyncDirtyFavoriteToolsWork() = enqueueUniqueWork(
    WORK_NAME,
    ExistingWorkPolicy.KEEP,
    SyncWorkRequestBuilder<SyncDirtyFavoriteToolsWorker>().build()
)

@HiltWorker
internal class SyncDirtyFavoriteToolsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val favoriteToolsSyncTasks: UserFavoriteToolsSyncTasks
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork() =
        if (favoriteToolsSyncTasks.syncDirtyFavoriteTools()) Result.success() else Result.retry()
}
