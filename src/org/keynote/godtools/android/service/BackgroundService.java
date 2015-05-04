package org.keynote.godtools.android.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.keynote.godtools.android.broadcast.BroadcastUtil;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.http.AuthTask;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;

import java.io.InputStream;

/**
 * Created by matthewfrederick on 5/4/15.
 */
public class BackgroundService extends IntentService implements AuthTask.AuthTaskHandler, MetaTask.MetaTaskHandler, DownloadTask.DownloadTaskHandler
{
    private final String TAG = getClass().getSimpleName();

    private static final String TYPE = "TYPE";
    private static final String PREFS_NAME = "GodTools";

    private static final int AUTHENTICATE_GENERIC = 0;

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
        broadcastManager.sendBroadcast(BroadcastUtil.startBroadcast());

        int type = intent.getIntExtra(TYPE, -1);
        switch (type)
        {
            case AUTHENTICATE_GENERIC:
                GodToolsApiClient.authenticateGeneric(this);
                break;
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

    public static void authenticateGeneric(Context context)
    {
        final Bundle extras = new Bundle(1);
        extras.putInt(TYPE, AUTHENTICATE_GENERIC);
        Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }

    @Override
    public void authComplete(String authorization)
    {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Authorization_Generic", authorization);
        editor.apply();
        Log.i(TAG, "Now Authorized");

        broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.AUTH));
    }

    @Override
    public void authFailed()
    {
        broadcastManager.sendBroadcast(BroadcastUtil.failBroadcast(Type.AUTH));
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag)
    {

    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
    {

    }

    @Override
    public void metaTaskComplete(InputStream is, String langCode, String tag)
    {

    }

    @Override
    public void metaTaskFailure(InputStream is, String langCode, String tag)
    {

    }
}
