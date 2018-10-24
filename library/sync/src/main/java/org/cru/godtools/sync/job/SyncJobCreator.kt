package org.cru.godtools.sync.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

class SyncJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        return when (tag) {
            FOLLOWUP_JOB_TAG -> SyncFollowupJob()
            SHARES_JOB_TAG -> SyncSharesJob()
            else -> null
        }
    }
}
