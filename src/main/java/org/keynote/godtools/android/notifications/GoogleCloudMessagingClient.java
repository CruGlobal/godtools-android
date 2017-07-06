package org.keynote.godtools.android.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.cru.godtools.analytics.AnalyticsService;
import org.keynote.godtools.android.service.BackgroundService;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.IOException;

/**
 * Created by ryancarlson on 7/20/15.
 */
public class GoogleCloudMessagingClient
{
    private static final String SENDER_ID = "237513440670";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private final Context context;
    private final SnuffyApplication application;
    private final Activity callingActivity;
    private final SharedPreferences userSettings;

    public static GoogleCloudMessagingClient getInstance(Context context,
                                                         SnuffyApplication application,
                                                         Activity callingActivity,
                                                         SharedPreferences userSettings)
    {
        return new GoogleCloudMessagingClient(context,application,callingActivity,userSettings);
    }

    private GoogleCloudMessagingClient(Context context, SnuffyApplication application, Activity callingActivity, SharedPreferences userSettings)
    {
        this.context = context;
        this.application = application;
        this.callingActivity = callingActivity;
        this.userSettings = userSettings;
    }

    public void registerForNotificationsIfNecessary(String tag)
    {
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (googlePlayServicesIsAvailable())
        {
            Log.i(tag, "Registering Device");

            String registrationId = NotificationUtilities.getStoredRegistrationId(context, userSettings);

            if (registrationId.isEmpty())
            {
                registerInBackground();
                // since when an app is first registered notifications are probably on,
                // send first state to Google Analytics
                AnalyticsService.getInstance(context).settingChanged("Notification State", "Turned ON");
            }
        }
        else
        {
            Log.i(tag, "No valid Google Play Services APK found.");
        }
    }

    private boolean googlePlayServicesIsAvailable()
    {
        final GoogleApiAvailability playServices = GoogleApiAvailability.getInstance();
        final int resultCode = playServices.isGooglePlayServicesAvailable(callingActivity);
        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        }

//        // XXX: we disable the error dialog since we should be able to work without GPS
//        if (playServices.isUserResolvableError(resultCode)) {
//            playServices.getErrorDialog(callingActivity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
//        }

        return false;
    }

    private void registerInBackground()
    {
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                String msg;
                try
                {
                    GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(context);
                    String registrationId = googleCloudMessaging.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + registrationId;

                    sendRegistrationIdToGodToolsAPI(registrationId);

                    NotificationUtilities.storeRegistrationId(context, userSettings, registrationId);
                } catch (IOException ex)
                {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToGodToolsAPI(String registrationId)
    {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        BackgroundService.registerDevice(context, registrationId, deviceId);
    }
}
