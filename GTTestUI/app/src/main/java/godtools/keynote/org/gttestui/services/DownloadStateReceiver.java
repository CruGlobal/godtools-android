package godtools.keynote.org.gttestui.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by rmatt on 2/28/2017.
 */
public abstract class DownloadStateReceiver extends BroadcastReceiver {
    // Prevents instantiation
    protected DownloadStateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int progress = (int) intent.getExtras().get(DownloadService.DOWNLOAD_EXTENDED_DATA_PROGRESS);
        downloadProgressUpdate(progress);
    }

    public abstract void downloadProgressUpdate(int i);
}
