package org.cru.godtools.sync.job;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class SyncJobCreator implements JobCreator {
    @Override
    public Job create(@NonNull final String tag) {
        switch (tag) {
            case SyncFollowupJobKt.FOLLOWUP_JOB_TAG:
                return new SyncFollowupJob();
            case SyncSharesJob.TAG:
                return new SyncSharesJob();
            default:
                return null;
        }
    }
}
