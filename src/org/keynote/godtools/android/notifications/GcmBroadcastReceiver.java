package org.keynote.godtools.android.notifications;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by matthewfrederick on 12/19/14.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver
{
    private static final String PREFS_NAME = "GodTools";
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificationsOn = settings.getBoolean("Notifications", true);
        
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
