package org.keynote.godtools.android.newnew.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class DownloadStateReceiver extends BroadcastReceiver {
    // Prevents instantiation
    protected DownloadStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean complete = intent.getExtras().getBoolean(DownloadService.DOWNLOAD_COMPLETED_KEY, false);
        if (complete) {
            boolean success = intent.getExtras().getBoolean(DownloadService.DOWNLOAD_SUCCESSFUL_KEY, false);
            DownloadServiceBO downloadServiceBO = intent.getExtras().getParcelable(DownloadService.DOWNLOAD_SERVICE_BO_KEY);
            if (success)
                success(downloadServiceBO);
            else
                fail(downloadServiceBO);
        } else {
            int progress = (int) intent.getExtras().get(DownloadService.DOWNLOAD_EXTENDED_DATA_PROGRESS);
            String languageCode = intent.getExtras().getString(DownloadService.DOWNLOAD_EXTENDED_DATA_LANGUAGE_CODE);
            progress(languageCode, progress);
        }
    }

    public abstract void progress(String languageCode, int progress);

    public abstract void fail(DownloadServiceBO downloadServiceBO);

    public abstract void success(DownloadServiceBO downloadServiceBO);
}
