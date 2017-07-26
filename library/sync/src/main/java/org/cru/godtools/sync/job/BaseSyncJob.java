package org.cru.godtools.sync.job;

import android.os.Build;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import static org.ccci.gto.android.common.TimeConstants.HOUR_IN_MS;
import static org.ccci.gto.android.common.TimeConstants.MIN_IN_MS;

abstract class BaseSyncJob extends Job {
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
}
