package org.cru.godtools.sync.job;

import android.support.annotation.NonNull;

import org.cru.godtools.sync.FollowupSyncTasks;

import java.io.IOException;

public final class SyncFollowupJob extends BaseSyncJob {
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

    public static void scheduleFollowupJob() {
        scheduleSyncJob(TAG);
    }
}
