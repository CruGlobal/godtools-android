package org.keynote.godtools.android.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.api.GodToolsApi;
import org.keynote.godtools.android.broadcast.BroadcastUtil;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.http.APITasks;
import org.keynote.godtools.android.notifications.NotificationInfo;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.keynote.godtools.android.utils.Constants.ACCESS_CODE;
import static org.keynote.godtools.android.utils.Constants.AUTH_CODE;
import static org.keynote.godtools.android.utils.Constants.AUTH_DRAFT;
import static org.keynote.godtools.android.utils.Constants.DEVICE_ID;
import static org.keynote.godtools.android.utils.Constants.NOTIFICATIONS_ON;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.REGISTRATION_ID;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;
import static org.keynote.godtools.android.utils.Constants.TYPE;

/**
 * Created by matthewfrederick on 5/4/15.
 */
public class BackgroundService extends IntentService
{
    private final String TAG = getClass().getSimpleName();

    private LocalBroadcastManager broadcastManager;
    private SharedPreferences settings;

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
            registerDeviceForNotifications(intent);
        }
        else if (APITasks.AUTHENTICATE_ACCESS_CODE.equals(intent.getSerializableExtra(TYPE)))
        {
            authenticateAccessCode(intent);
        }
        else if (APITasks.VERIFY_ACCESS_CODE.equals(intent.getSerializableExtra(TYPE)))
        {
            verifyAuthToken(intent);
        }
    }

    private static Intent baseIntent(Context context, Bundle extras)
    {
        Intent intent = new Intent(context, BackgroundService.class);
        if (extras != null)
        {
            intent.putExtras(extras);
        }
        return intent;
    }

    private void registerDeviceForNotifications(@NonNull final Intent intent) {
        try {
            final String regId = intent.getStringExtra(REGISTRATION_ID);
            final Response<ResponseBody> response = GodToolsApi.INSTANCE
                    .registerDeviceForNotifications(intent.getStringExtra(REGISTRATION_ID),
                                                    intent.getStringExtra(DEVICE_ID),
                                                    intent.getBooleanExtra(NOTIFICATIONS_ON, true)).execute();

            if (response.isSuccessful()) {
                registrationComplete(regId);
            } else {
                registrationFailed();
            }
        } catch (final IOException e) {
            registrationFailed();
        }
    }

    private void authenticateAccessCode(@NonNull final Intent intent) {
        try {
            // get an auth token for the specified access_code
            final Response<ResponseBody> response =
                    GodToolsApi.INSTANCE.getAuthToken(intent.getStringExtra(ACCESS_CODE)).execute();

            // a 204 response is successful, auth_token is in the Authorization header
            if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                authComplete(response.headers().get(HttpHeaders.AUTHORIZATION), true, false);
            }
            // otherwise we failed
            else {
                authFailed(true, false);
            }
        } catch (final IOException e) {
            // any IOException should be considered a failure currently
            authFailed(true, false);
        }
    }

    private void verifyAuthToken(@NonNull final Intent intent) {
        try {
            // verify that the specified auth_token is still valid
            final String authToken = intent.getStringExtra(ACCESS_CODE);
            final Response<ResponseBody> response = GodToolsApi.INSTANCE.verifyAuthToken(authToken).execute();

            // a 204 response is successful
            if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                authComplete(authToken, false, true);
            }
            // otherwise we failed
            else {
                authFailed(false, true);
            }
        } catch (final IOException e) {
            authFailed(false, true);
        }
    }

    public static void registerDevice(Context context, String regId, String deviceID)
    {
        registerDevice(context, regId, deviceID, true);
    }

    public static void registerDevice(Context context, String regId, String deviceID, boolean notificationsOn)
    {
        final Bundle extras = new Bundle(4);
        extras.putSerializable(TYPE, APITasks.REGISTER_DEVICE);
        extras.putString(REGISTRATION_ID, regId);
        extras.putString(DEVICE_ID, deviceID);
        extras.putBoolean(NOTIFICATIONS_ON, notificationsOn);
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

    private void authComplete(String authorization, boolean authenticateAccessCode, boolean verifyStatus)
    {
        Log.i(TAG, "Now Authorized");

        if (authenticateAccessCode)
        {
            if (!Strings.isNullOrEmpty(authorization))
            {
                settings.edit()
                        .putString(AUTH_DRAFT, authorization)
                        .putBoolean(TRANSLATOR_MODE, true)
                        .apply();

                broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.ENABLE_TRANSLATOR));
            }
        }
        else if (verifyStatus)
        {
            broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.ENABLE_TRANSLATOR));
        }
        else
        {
            settings.edit().putString(AUTH_CODE, authorization).apply();

            broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.AUTH));
        }
    }

    private void authFailed(boolean authenticateAccessCode, boolean verifyStatus)
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

    private void registrationComplete(String regId)
    {
        Log.i(NotificationInfo.NOTIFICATION_TAG, "API Registration Complete");
    }

    private void registrationFailed()
    {
        Log.i(NotificationInfo.NOTIFICATION_TAG, "API Registration Failed");
    }
}
