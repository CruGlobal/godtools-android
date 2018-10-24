package org.cru.godtools.sync.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import org.ccci.gto.android.common.base.TimeConstants.HOUR_IN_MS
import org.ccci.gto.android.common.base.TimeConstants.MIN_IN_MS

internal fun scheduleSyncJob(tag: String) {
    JobRequest.Builder(tag)
            .setExecutionWindow(15 * MIN_IN_MS, 4 * HOUR_IN_MS)
            .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
            .setRequirementsEnforced(true)
            .setUpdateCurrent(true)
            .build()
            .schedule()
}

internal abstract class BaseSyncJob : Job()
