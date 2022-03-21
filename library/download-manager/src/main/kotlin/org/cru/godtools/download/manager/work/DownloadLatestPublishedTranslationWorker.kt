package org.cru.godtools.download.manager.work

import android.content.Context
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
import org.ccci.gto.android.common.androidx.work.getLocale
import org.ccci.gto.android.common.androidx.work.putLocale
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.TranslationKey

private const val WORK_NAME = "DownloadTranslation"
private const val TAG_DOWNLOAD_MANAGER = "DownloadManager"
private const val KEY_TRANSLATION = "translation"

internal fun WorkManager.scheduleDownloadTranslationWork(key: TranslationKey) = enqueueUniqueWork(
    key.workName,
    ExistingWorkPolicy.KEEP,
    OneTimeWorkRequestBuilder<DownloadLatestPublishedTranslationWorker>()
        .addTag(TAG_DOWNLOAD_MANAGER)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
        )
        .setInputData(
            Data.Builder()
                .putTranslationKey(KEY_TRANSLATION, key)
                .build()
        )
        .build()
)

@HiltWorker
internal class DownloadLatestPublishedTranslationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val downloadManager: GodToolsDownloadManager
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val key = inputData.getTranslationKey(KEY_TRANSLATION)
        return if (downloadManager.downloadLatestPublishedTranslation(key)) Result.success() else Result.retry()
    }
}

private val TranslationKey.workName get() = "$WORK_NAME:$tool:${locale?.toLanguageTag()}"

private fun Data.Builder.putTranslationKey(key: String, translation: TranslationKey) =
    putString("$key:tool", translation.tool).putLocale("$key:locale", translation.locale)
private fun Data.getTranslationKey(key: String) = TranslationKey(getString("$key:tool"), getLocale("$key:locale"))
