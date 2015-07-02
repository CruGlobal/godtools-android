package org.keynote.godtools.android.notifications;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.Splash;

import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;

/**
 * GCM Intent Service
 */
@SuppressWarnings("StatementWithEmptyBody")
public class GcmIntentService extends IntentService
{
    public static final int NOTIFICATION_ID = 1;
    public static final String TAG = GcmIntentService.class.getSimpleName();
    Intent resultIntent;

    public GcmIntentService()
    {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty())
        {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
            {

            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
            {

            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                int type;
                try
                {
                    type = Integer.parseInt(extras.getString("type"));
                } catch (Exception e)
                {
                    type = 0;
                }

                Log.i(TAG, "Creating Notification");
                // Post notification of received message.
                sendNotification(extras.getString("msg"), type);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, int type)
    {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.homescreen_godtools_logo)
                .setContentTitle(PREFS_NAME)
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setAutoCancel(true);


        if (type == NotificationInfo.DAY_AFTER_SHARE)
        {
            resultIntent = new Intent(Intent.ACTION_SEND);
            resultIntent.setType("text/plain");
        }
        else if (type == NotificationInfo.AFTER_1_PRESENTATION || type == NotificationInfo.AFTER_10_PRESENTATIONS || type == NotificationInfo.AFTER_3_USES)
        {
            resultIntent = new Intent(Intent.ACTION_SENDTO);
            String uriText = "mailto:" + Uri.encode("support@godtools.com");
            Uri uri = Uri.parse(uriText);
            resultIntent.setData(uri);
        }
        else
        {
            resultIntent = new Intent(this, Splash.class);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        Log.i(TAG, "Showing notification");
    }
}
