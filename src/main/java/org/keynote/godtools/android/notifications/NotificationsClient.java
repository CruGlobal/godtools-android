package org.keynote.godtools.android.notifications;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.cru.godtools.api.GodToolsApi;
import org.cru.godtools.api.model.GTNotificationRegister;
import org.keynote.godtools.android.utils.Constants;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        String authorization = userSettings.getString(Constants.AUTH_CODE,"");
        String registrationId = NotificationUtilities.getStoredRegistrationId(context, userSettings);

        // send notification update each time app is used for notification type 1
        GTNotificationRegister gtNotificationRegister = new GTNotificationRegister(registrationId, NotificationInfo.NOT_USED_2_WEEKS);
        GodToolsApi.getInstance(context).legacy.updateNotification(authorization, gtNotificationRegister)
                .enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful())
                {
                    Log.i(NotificationInfo.NOTIFICATION_TAG, "Used Notification notice sent to API");
                }
                else
                {
                    Log.e(NotificationInfo.NOTIFICATION_TAG, "Used notification notice failed to send to API");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
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
        String authorization = userSettings.getString(Constants.AUTH_CODE,"");
        String registrationId = NotificationUtilities.getStoredRegistrationId(context, userSettings);
        GTNotificationRegister gtNotificationRegister = new GTNotificationRegister(registrationId, NotificationInfo.AFTER_3_USES);
        GodToolsApi.getInstance(context).legacy.updateNotification(authorization, gtNotificationRegister)
                .enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful())
                {
                    Log.i(NotificationInfo.NOTIFICATION_TAG, "3 Uses Notification notice sent to API");
                }
                else
                {
                    Log.e(NotificationInfo.NOTIFICATION_TAG, "3 Uses notification notice failed to send to API");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(NotificationInfo.NOTIFICATION_TAG, "3 Uses notification notice failed to send to API");
                t.printStackTrace();

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
