package org.cru.godtools.sync.job

import com.evernote.android.job.Job
import org.cru.godtools.sync.task.ToolSyncTasks

internal const val SHARES_JOB_TAG = "sync_shares"

fun scheduleSharesJob() {
    scheduleSyncJob(SHARES_JOB_TAG)
}

internal class SyncSharesJob : BaseSyncJob() {
    override fun onRunJob(params: Job.Params): Job.Result {
        val result = ToolSyncTasks(context).syncShares()
        return if (result) Job.Result.SUCCESS else Job.Result.RESCHEDULE
    }
}
