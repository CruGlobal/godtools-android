package org.cru.godtools.sync.job;

import android.support.annotation.NonNull;

import org.cru.godtools.sync.task.ToolSyncTasks;

public final class SyncSharesJob extends BaseSyncJob {
    static final String TAG = "sync_shares";

    @NonNull
    @Override
    protected Result onRunJob(final Params params) {
        final boolean result = new ToolSyncTasks(getContext()).syncShares();
        return result ? Result.SUCCESS : Result.RESCHEDULE;
    }

    public static void scheduleSharesJob() {
        scheduleSyncJob(TAG);
    }
}
