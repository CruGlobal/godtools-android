package org.cru.godtools.downloadmanager.work

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.cru.godtools.downloadmanager.GodToolsDownloadManager

@VisibleForTesting
internal const val WORK_NAME_DOWNLOAD_ATTACHMENT = "DownloadAttachment"
private const val KEY_ATTACHMENT_ID = "attachmentId"

internal fun WorkManager.scheduleDownloadAttachmentWork(id: Long) = enqueueUniqueWork(
    "$WORK_NAME_DOWNLOAD_ATTACHMENT:$id",
    ExistingWorkPolicy.KEEP,
    OneTimeWorkRequestBuilder<DownloadAttachmentWorker>()
        .addTag(TAG_DOWNLOAD_MANAGER)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
        )
        .setInputData(
            Data.Builder()
                .putLong(KEY_ATTACHMENT_ID, id)
                .build()
        )
        .build()
)

@HiltWorker
internal class DownloadAttachmentWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val downloadManager: GodToolsDownloadManager
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val attachmentId = inputData.getLong(KEY_ATTACHMENT_ID, -1)
        return if (downloadManager.downloadAttachment(attachmentId)) Result.success() else Result.retry()
    }
}
