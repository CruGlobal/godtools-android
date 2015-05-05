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
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.http.AuthTask;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import static org.keynote.godtools.android.utils.Constants.AUTHENTICATE_GENERIC;
import static org.keynote.godtools.android.utils.Constants.AUTH_CODE;
import static org.keynote.godtools.android.utils.Constants.GET_LIST_OF_PACKAGES;
import static org.keynote.godtools.android.utils.Constants.META;
import static org.keynote.godtools.android.utils.Constants.META_IS;
import static org.keynote.godtools.android.utils.Constants.META_LANG_CODE;
import static org.keynote.godtools.android.utils.Constants.META_TAG;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.TYPE;

/**
 * Created by matthewfrederick on 5/4/15.
 */
public class BackgroundService extends IntentService implements AuthTask.AuthTaskHandler, MetaTask.MetaTaskHandler, DownloadTask.DownloadTaskHandler
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
        broadcastManager.sendBroadcast(BroadcastUtil.startBroadcast());
        Log.i(TAG, "Action Started");

        int type = intent.getIntExtra(TYPE, -1);
        switch (type)
        {
            case AUTHENTICATE_GENERIC:
                GodToolsApiClient.authenticateGeneric(this);
                break;
            case GET_LIST_OF_PACKAGES:
                GodToolsApiClient.getListOfPackages(settings.getString(AUTH_CODE, ""), META, this);
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

    public static void getListOfPackages(Context context)
    {
        final Bundle extras = new Bundle(1);
        extras.putInt(TYPE, GET_LIST_OF_PACKAGES);
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

        broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.AUTH, null));
    }

    @Override
    public void authFailed()
    {
        broadcastManager.sendBroadcast(BroadcastUtil.failBroadcast(Type.FAIL));
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag)
    {

    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
    {
        broadcastManager.sendBroadcast(BroadcastUtil.failBroadcast(Type.FAIL));
    }

    @Override
    public void metaTaskComplete(InputStream is, String langCode, String tag)
    {
        List<GTLanguage> languageList = GTPackageReader.processMetaResponse(is);
        Bundle extras = new Bundle(3);
        extras.putSerializable(META_IS, (Serializable) languageList);
        extras.putString(META_LANG_CODE, langCode);
        extras.putString(META_TAG, tag);

        broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.META_TASK, extras));
    }

    @Override
    public void metaTaskFailure(InputStream is, String langCode, String tag)
    {
        broadcastManager.sendBroadcast(BroadcastUtil.failBroadcast(Type.FAIL));
    }
}
