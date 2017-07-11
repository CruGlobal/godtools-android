package org.cru.godtools.sync.job;

import android.os.Build;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.evernote.android.job.JobRequest;

import org.cru.godtools.sync.FollowupSyncTasks;
import org.cru.godtools.sync.ToolSyncTasks;

import java.io.IOException;

import static org.ccci.gto.android.common.TimeConstants.HOUR_IN_MS;
import static org.ccci.gto.android.common.TimeConstants.MIN_IN_MS;

public class SyncJobCreator implements JobCreator {
    @Override
    public Job create(@NonNull final String tag) {
        switch (tag) {
            case SyncFollowupJob.TAG:
                return new SyncFollowupJob();
            case SyncSharesJob.TAG:
                return new SyncSharesJob();
            default:
                return null;
        }
    }

    static void scheduleSyncJob(final String tag) {
        new JobRequest.Builder(tag)
                .setExecutionWindow(15 * MIN_IN_MS, 4 * HOUR_IN_MS)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .setUpdateCurrent(true)
                .setPersisted(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                .build()
                .schedule();
    }

    static final class SyncFollowupJob extends Job {
        static final String TAG = "sync_followup";

        @NonNull
        @Override
        protected Result onRunJob(final Params params) {
            try {
                new FollowupSyncTasks(getContext()).syncFollowups();
            } catch (IOException e) {
                return Result.RESCHEDULE;
            }
            return Result.SUCCESS;
        }

        public static void scheduleJob() {
            SyncJobCreator.scheduleSyncJob(TAG);
        }
    }

    static final class SyncSharesJob extends Job {
        static final String TAG = "sync_shares";

        @NonNull
        @Override
        protected Result onRunJob(final Params params) {
            final boolean result = new ToolSyncTasks(getContext()).syncShares();
            return result ? Result.SUCCESS : Result.RESCHEDULE;
        }

        public static void scheduleJob() {
            SyncJobCreator.scheduleSyncJob(TAG);
        }
    }
}
