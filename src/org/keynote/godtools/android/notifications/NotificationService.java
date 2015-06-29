package org.keynote.godtools.android.notifications;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.common.base.Strings;

import org.keynote.godtools.android.googleAnalytics.EventTracker;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.NotificationUpdateTask;
import org.keynote.godtools.android.service.BackgroundService;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.IOException;

import static org.keynote.godtools.android.utils.Constants.APP_VERSION;
import static org.keynote.godtools.android.utils.Constants.AUTH_GENERIC;
import static org.keynote.godtools.android.utils.Constants.EMPTY_STRING;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.REGISTRATION_ID;

/**
 * Class used for setting up GCM for application.
 */
public class NotificationService extends IntentService
{
    private static final String TAG = NotificationService.class.getSimpleName();

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private String regid = EMPTY_STRING;
    private SharedPreferences settings;
    private GoogleCloudMessaging gcm;
    private static Activity callingActivity;

    public NotificationService()
    {
        super(TAG);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.i(TAG, regid);

        if (checkPlayServices())
        {
            Log.i(TAG, "Registering Device");
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId();

            if (regid.isEmpty())
            {
                register();
                EventTracker.track(getApp(), "HomeScreen", "Notification State", "Turned ON");
            }

            GodToolsApiClient.updateNotification(settings.getString(AUTH_GENERIC, EMPTY_STRING),
                    regid, NotificationInfo.NOT_USED_2_WEEKS, new NotificationUpdateTask.NotificationUpdateTaskHandler()
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
        else
        {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

        Log.i(TAG, regid);
    }

    public static void registerDevice(Context context, Activity activity)
    {
        callingActivity = activity;

        Intent intent = new Intent(context, NotificationService.class);
        context.startService(intent);
    }


    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, callingActivity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId()
    {

        String registrationId = settings.getString(REGISTRATION_ID, EMPTY_STRING);
        if (Strings.isNullOrEmpty(registrationId))
        {
            Log.i(TAG, "Registration not found.");
            return EMPTY_STRING;
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = settings.getInt(APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion)
        {
            Log.i(TAG, "App version changed.");
            return EMPTY_STRING;
        }
        return registrationId;
    }

    private int getAppVersion()
    {
        try
        {
            PackageInfo packageInfo = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e)
        {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void register()
    {
        String SENDER_ID = "237513440670";

        String msg;
        try
        {
            if (gcm == null)
            {
                gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            }
            regid = gcm.register(SENDER_ID);
            msg = "Device registered, registration ID = " + regid;

            sendRegistrationIdToBackend();

            // Persist the regID - no need to register again.
            storeRegistrationId(regid);
        } catch (IOException ex)
        {
            msg = "Error :" + ex.getMessage();
        }

        Log.i(TAG, msg);
    }

    private void sendRegistrationIdToBackend()
    {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        BackgroundService.registerDevice(getApplicationContext(), regid, deviceId);
    }

    private void storeRegistrationId(String regId)
    {
        int appVersion = getAppVersion();
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(REGISTRATION_ID, regId);
        editor.putInt(APP_VERSION, appVersion);
        editor.apply();
    }

    private SnuffyApplication getApp()
    {
        return (SnuffyApplication) getApplication();
    }
}
