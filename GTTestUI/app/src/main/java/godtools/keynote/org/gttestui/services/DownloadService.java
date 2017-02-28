package godtools.keynote.org.gttestui.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by rmatt on 2/28/2017.
 */

public class DownloadService extends IntentService {

    public static final String DOWNLOAD_BROADCAST_ACTION =
            "godtools.keynote.org.gttestui.services.download_broadcast";

    // Defines the key for the status "extra" in an Intent
    public static final String DOWNLOAD_EXTENDED_DATA_PROGRESS =
            "godtools.keynote.org.gttestui.services.download_progress";



    public DownloadService()
    {
        super("DownloadService");
    }

    public DownloadService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        String dataString = workIntent.getDataString();

        for (int i = 0; i < 100; i+=2) {
            //Sample reporting
            try {
                Thread.sleep(100);

                Intent localIntent =
                        new Intent(DownloadService.DOWNLOAD_BROADCAST_ACTION)
                                // Puts the status into the Intent
                                .putExtra(DOWNLOAD_EXTENDED_DATA_PROGRESS, i);
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
