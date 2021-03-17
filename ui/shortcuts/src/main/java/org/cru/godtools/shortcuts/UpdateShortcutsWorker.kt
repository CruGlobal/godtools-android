package org.cru.godtools.shortcuts

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.google.firebase.perf.ktx.trace
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val WORK_NAME = "UpdateShortcuts"

internal fun WorkManager.scheduleUpdateShortcutsWork() =
    enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<UpdateShortcutsWorker>().build())

@HiltWorker
class UpdateShortcutsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val shortcutManager: GodToolsShortcutManager
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = Firebase.performance.newTrace("UpdateShortcutsWorker.doWork()").trace {
        shortcutManager.refreshShortcutsNow()
        Result.success()
    }
}
