package org.keynote.godtools.android.notifications;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.NotificationUpdateTask;
import org.keynote.godtools.android.utils.Constants;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ryancarlson on 7/20/15.
 */
public class NotificationsClient
{

    public static NotificationsClient getInstance(Context context, SharedPreferences userSettings)
    {
        return new NotificationsClient(context, userSettings);
    }

    private NotificationsClient(Context context, SharedPreferences userSettings)
    {
        this.context = context;
        this.userSettings = userSettings;
    }

    private final Context context;
    private final SharedPreferences userSettings;

    public void sendLastUsageUpdateToGodToolsAPI()
    {
        String authorization = userSettings.getString(Constants.AUTH_GENERIC, Constants.EMPTY_STRING);
        String registrationId = NotificationUtilities.getStoredRegistrationId(context, userSettings);

        // send notification update each time app is used for notification type 1
        GodToolsApiClient.updateNotification(authorization,registrationId, NotificationInfo.NOT_USED_2_WEEKS, new NotificationUpdateTask.NotificationUpdateTaskHandler()
                {
                    @Override
                    public void registrationComplete(String regId)
                    {
                        Log.i(NotificationInfo.NOTIFICATION_TAG, "Used Notification notice sent to API");
                    }

                    @Override
                    public void registrationFailed()
                    {
                        Log.e(NotificationInfo.NOTIFICATION_TAG, "Used notification notice failed to send to API");
                    }
                });
    }

    public void startTimerForTrackingAppUsageTime()
    {
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                if(isAppInForeground())
                {
                    sendNumberOfUsagesUpdateToGodToolsAPI();
                }
            }
        };

        Timer timer = new Timer("1.5MinuteTimer");
        timer.schedule(timerTask, 90000); //1.5 minutes
    }

    private void sendNumberOfUsagesUpdateToGodToolsAPI()
    {
        String authorization = userSettings.getString(Constants.AUTH_GENERIC, Constants.EMPTY_STRING);
        String registrationId = NotificationUtilities.getStoredRegistrationId(context, userSettings);

        GodToolsApiClient.updateNotification(authorization, registrationId, NotificationInfo.AFTER_3_USES, new NotificationUpdateTask.NotificationUpdateTaskHandler()
        {
            @Override
            public void registrationComplete(String regId)
            {
                Log.i(NotificationInfo.NOTIFICATION_TAG, "3 Uses Notification notice sent to API");
            }

            @Override
            public void registrationFailed()
            {
                Log.e(NotificationInfo.NOTIFICATION_TAG, "3 Uses notification notice failed to send to API");
            }
        });
    }

    private boolean isAppInForeground()
    {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(1);

        return (services.get(0).topActivity.getPackageName()
                .equalsIgnoreCase(context.getPackageName()));
    }
}

