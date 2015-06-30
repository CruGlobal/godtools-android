package org.keynote.godtools.android.notifications;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;

import static org.keynote.godtools.android.utils.Constants.NOTIFICATIONS;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;

/**
 * Google Cloud Messages receiver
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver
{
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificationsOn = settings.getBoolean(NOTIFICATIONS, true);
        
        if (notificationsOn)
        {
            // Explicitly specify that GcmIntentService will handle the intent.
            ComponentName comp = new ComponentName(context.getPackageName(),
                    GcmIntentService.class.getName());
            // Start the service, keeping the device awake while it is launching.
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        }
    }
}
