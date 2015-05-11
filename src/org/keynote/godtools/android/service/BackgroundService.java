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
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.http.APITasks;
import org.keynote.godtools.android.http.AuthTask;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.InputStream;

import static org.keynote.godtools.android.utils.Constants.AUTH_CODE;
import static org.keynote.godtools.android.utils.Constants.BACKGROUND_TASK_TAG;
import static org.keynote.godtools.android.utils.Constants.LANG_CODE;
import static org.keynote.godtools.android.utils.Constants.META;
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
        broadcastManager.sendBroadcast(BroadcastUtil.startBroadcast());
        Log.i(TAG, "Action Started: " + intent.getSerializableExtra(TYPE));


        if (APITasks.AUTHENTICATE_GENERIC.equals(intent.getSerializableExtra(TYPE)))
        {
            GodToolsApiClient.authenticateGeneric(this);
        }
        else if (APITasks.GET_LIST_OF_PACKAGES.equals(intent.getSerializableExtra(TYPE)))
        {
            GodToolsApiClient.getListOfPackages(settings.getString(AUTH_CODE, ""),
                    META, this);
        }
        else if (APITasks.GET_LIST_OF_DRAFTS.equals(intent.getSerializableExtra(TYPE)))
        {
            GodToolsApiClient.getListOfDrafts(settings.getString(AUTH_CODE, ""),
                    intent.getStringExtra(LANG_CODE),
                    intent.getStringExtra(BACKGROUND_TASK_TAG), this);
        }
        else if (APITasks.DOWNLOAD_LANGUAGE_PACK.equals(intent.getSerializableExtra(TYPE)))
        {
            GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                    intent.getStringExtra(LANG_CODE),
                    intent.getStringExtra(BACKGROUND_TASK_TAG),
                    settings.getString(AUTH_CODE, ""), this);
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

    public static void firstSetup(SnuffyApplication app)
    {
        BackgroundService service = new BackgroundService();
        service.initialContentTask(app);
    }

    private void initialContentTask(SnuffyApplication app)
    {
        PrepareInitialContentTask.run(app.getApplicationContext(), app.getDocumentsDir());
    }

    public static void authenticateGeneric(Context context)
    {
        final Bundle extras = new Bundle(1);
        extras.putSerializable(TYPE, APITasks.AUTHENTICATE_GENERIC);
        Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }

    public static void getListOfPackages(Context context)
    {
        final Bundle extras = new Bundle(1);
        extras.putSerializable(TYPE, APITasks.GET_LIST_OF_PACKAGES);
        Intent intent = baseIntent(context, extras);
        context.startService(intent);
    }

    public static void downloadLanguagePack(Context context, String langCode, String tag)
    {
        final Bundle extras = new Bundle(3);
        extras.putSerializable(TYPE, APITasks.DOWNLOAD_LANGUAGE_PACK);
        extras.putString(LANG_CODE, langCode);
        extras.putString(BACKGROUND_TASK_TAG, tag);
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
        Log.i(TAG, "Download Complete");
        DownloadService.downloadComplete(langCode, tag, BackgroundService.this, (SnuffyApplication) getApplication());
        broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.DOWNLOAD_TASK));
    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
    {
        broadcastManager.sendBroadcast(BroadcastUtil.failBroadcast(Type.DOWNLOAD_TASK));
    }

    @Override
    public void metaTaskComplete(InputStream is, String langCode, String tag)
    {
        Log.i(TAG, "Update Package List Task");
        // this will cause download task to be run.
        UpdatePackageListTask.run(is, adapter, isFirstLaunch(), (SnuffyApplication) getApplication(),
                languagePrimary, languageParallel, BackgroundService.this);

        broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.META_TASK));
    }

    @Override
    public void metaTaskFailure(InputStream is, String langCode, String tag)
    {
        broadcastManager.sendBroadcast(BroadcastUtil.failBroadcast(Type.META_TASK));
    }

    private boolean isFirstLaunch()
    {
        return settings.getBoolean("firstLaunch", true);
    }
}
