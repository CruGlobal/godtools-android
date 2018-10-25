package org.cru.godtools.sync.job

import com.evernote.android.job.Job
import org.cru.godtools.sync.task.FollowupSyncTasks
import java.io.IOException

internal const val FOLLOWUP_JOB_TAG = "sync_followup"

fun scheduleFollowupJob() {
    scheduleSyncJob(FOLLOWUP_JOB_TAG)
}

internal class SyncFollowupJob : BaseSyncJob() {
    override fun onRunJob(params: Job.Params): Job.Result {
        return try {
            FollowupSyncTasks(context).syncFollowups()
            Job.Result.SUCCESS
        } catch (e: IOException) {
            Job.Result.RESCHEDULE
        }
    }
}
