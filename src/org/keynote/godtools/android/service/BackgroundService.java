package org.keynote.godtools.android.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.broadcast.BroadcastUtil;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.http.APITasks;
import org.keynote.godtools.android.http.AuthTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.NotificationRegistrationTask;
import org.keynote.godtools.android.notifications.NotificationInfo;

import static org.keynote.godtools.android.utils.Constants.ACCESS_CODE;
import static org.keynote.godtools.android.utils.Constants.AUTH_DRAFT;
import static org.keynote.godtools.android.utils.Constants.AUTH_GENERIC;
import static org.keynote.godtools.android.utils.Constants.DEVICE_ID;
import static org.keynote.godtools.android.utils.Constants.NOTIFICATIONS_ON;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.REGISTRATION_ID;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;
import static org.keynote.godtools.android.utils.Constants.TYPE;

/**
 * Background service class used to interact with GodTools API
 */
public class BackgroundService extends IntentService implements AuthTask.AuthTaskHandler,
        NotificationRegistrationTask.NotificationTaskHandler
{
    private final String TAG = getClass().getSimpleName();

    private LocalBroadcastManager broadcastManager;
    private SharedPreferences settings;
    private String languagePrimary;
    private String languageParallel;

    private DBAdapter adapter;

    public BackgroundService()
    {
        super("BackgroundService");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        languageParallel = settings.getString(GTLanguage.KEY_PARALLEL, "");
        adapter = DBAdapter.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // don't show the loading icon for registering device.
        if (!APITasks.REGISTER_DEVICE.equals(intent.getSerializableExtra(TYPE)))
            broadcastManager.sendBroadcast(BroadcastUtil.startBroadcast());

        Log.i(TAG, "Action Started: " + intent.getSerializableExtra(TYPE));

        if (APITasks.REGISTER_DEVICE.equals(intent.getSerializableExtra(TYPE)))
        {
            GodToolsApiClient.registerDeviceForNotifications(
                    intent.getStringExtra(REGISTRATION_ID),
                    intent.getStringExtra(DEVICE_ID),
                    intent.getStringExtra(NOTIFICATIONS_ON), this);
        }
        else if (APITasks.AUTHENTICATE_ACCESS_CODE.equals(intent.getSerializableExtra(TYPE)))
        {
            GodToolsApiClient.authenticateAccessCode(intent.getStringExtra(ACCESS_CODE), this);
        }
        else if (APITasks.VERIFY_ACCESS_CODE.equals(intent.getSerializableExtra(TYPE)))
        {
            GodToolsApiClient.verifyStatusOfAuthToken(intent.getStringExtra(ACCESS_CODE), this);
        }
    }

    public static Intent baseIntent(Context context, Bundle extras)
    {
        Intent intent = new Intent(context, BackgroundService.class);
        if (extras != null)
        {
            intent.putExtras(extras);
        }
        return intent;
    }

    public static void registerDevice(Context context, String regId, String deviceID)
    {
        registerDevice(context, regId, deviceID, "TRUE");
    }

    public static void registerDevice(Context context, String regId, String deviceID, String notificationsOn)
    {
        final Bundle extras = new Bundle(4);
        extras.putSerializable(TYPE, APITasks.REGISTER_DEVICE);
        extras.putString(REGISTRATION_ID, regId);
        extras.putString(DEVICE_ID, deviceID);
        extras.putString(NOTIFICATIONS_ON, notificationsOn);
        Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }

    public static void authenticateAccessCode(Context context, String accessCode)
    {
        final Bundle extras = new Bundle(2);
        extras.putSerializable(TYPE, APITasks.AUTHENTICATE_ACCESS_CODE);
        extras.putString(ACCESS_CODE, accessCode);
        Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }

    public static void verifyStatusOfAuthToken(Context context, String accessCode)
    {
        final Bundle extras = new Bundle(2);
        extras.putSerializable(TYPE, APITasks.VERIFY_ACCESS_CODE);
        extras.putString(ACCESS_CODE, accessCode);
        Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }

    @Override
    public void authComplete(String authorization, boolean authenticateAccessCode, boolean verifyStatus)
    {
        Log.i(TAG, "Now Authorized");

        if (authenticateAccessCode)
        {
            if (!Strings.isNullOrEmpty(authorization))
            {
                settings.edit().putString(AUTH_DRAFT, authorization).apply();
                settings.edit().putBoolean(TRANSLATOR_MODE, true).apply();

                broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.ENABLE_TRANSLATOR));
            }
        }
        else if (verifyStatus)
        {
            broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.ENABLE_TRANSLATOR));
        }
        else
        {
            settings.edit().putString(AUTH_GENERIC, authorization).apply();

            broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.AUTH));
        }
    }

    @Override
    public void authFailed(boolean authenticateAccessCode, boolean verifyStatus)
    {
        Log.i(TAG, "Auth Failed");

        if (authenticateAccessCode)
        {
            settings.edit().putString(AUTH_DRAFT, null).apply();
            Toast.makeText(BackgroundService.this, getString(R.string.wrong_passcode), Toast.LENGTH_SHORT).show();
        }
        else if (verifyStatus)
        {
            settings.edit().putString(AUTH_DRAFT, null).apply();
            Toast.makeText(BackgroundService.this, getString(R.string.expired_passcode), Toast.LENGTH_LONG).show();
        }

        broadcastManager.sendBroadcast(BroadcastUtil.failBroadcast(Type.AUTH));
    }

    @Override
    public void registrationComplete(String regId)
    {
        Log.i(NotificationInfo.NOTIFICATION_TAG, "API Registration Complete");
    }

    @Override
    public void registrationFailed()
    {
        Log.i(NotificationInfo.NOTIFICATION_TAG, "API Registration Failed");
    }
}
