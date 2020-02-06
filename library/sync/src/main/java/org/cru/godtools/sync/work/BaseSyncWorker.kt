package org.cru.godtools.sync.work

import androidx.work.Constraints
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder

internal const val TAG_SYNC = "sync"

internal inline fun <reified W : ListenableWorker> SyncWorkRequestBuilder() =
    OneTimeWorkRequestBuilder<W>()
        .addTag(TAG_SYNC)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
