package org.cru.godtools.sync.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.cru.godtools.sync.task.LanguagesSyncTasks

private const val WORK_NAME = "SyncTools"

internal fun WorkManager.scheduleSyncLanguagesWork() =
    enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, SyncWorkRequestBuilder<SyncLanguagesWorker>().build())

@HiltWorker
internal class SyncLanguagesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val languageSyncTasks: LanguagesSyncTasks
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork() = if (languageSyncTasks.syncLanguages()) Result.success() else Result.retry()
}
