package org.keynote.godtools.android.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.keynote.godtools.android.broadcast.BroadcastUtil;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.http.AuthTask;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.keynote.godtools.android.utils.Constants.AUTHENTICATE_GENERIC;
import static org.keynote.godtools.android.utils.Constants.AUTH_CODE;
import static org.keynote.godtools.android.utils.Constants.BACKGROUND_TASK_TAG;
import static org.keynote.godtools.android.utils.Constants.DOWNLOAD_LANGUAGE_PACK;
import static org.keynote.godtools.android.utils.Constants.GET_LIST_OF_DRAFTS;
import static org.keynote.godtools.android.utils.Constants.GET_LIST_OF_PACKAGES;
import static org.keynote.godtools.android.utils.Constants.KEY_NEW_LANGUAGE;
import static org.keynote.godtools.android.utils.Constants.KEY_UPDATE_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.KEY_UPDATE_PRIMARY;
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
        Log.i(TAG, "Action Started: " + intent.getIntExtra(TYPE, -1));

        int type = intent.getIntExtra(TYPE, -1);
        switch (type)
        {
            case AUTHENTICATE_GENERIC:
                GodToolsApiClient.authenticateGeneric(this);
                break;
            case GET_LIST_OF_PACKAGES:
                GodToolsApiClient.getListOfPackages(settings.getString(AUTH_CODE, ""),
                        META, this);
                break;
            case GET_LIST_OF_DRAFTS:
                GodToolsApiClient.getListOfDrafts(settings.getString(AUTH_CODE, ""),
                        intent.getStringExtra(LANG_CODE),
                        intent.getStringExtra(BACKGROUND_TASK_TAG), this);
                break;
            case DOWNLOAD_LANGUAGE_PACK:
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
        new PrepareInitialContentTask(app).execute((Void) null);
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

    public static void downloadLanguagePack(Context context, String langCode, String tag)
    {
        final Bundle extras = new Bundle(3);
        extras.putInt(TYPE, DOWNLOAD_LANGUAGE_PACK);
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
        new UpdatePackageListTask().doInBackground(is);

        broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.META_TASK));
    }

    @Override
    public void metaTaskFailure(InputStream is, String langCode, String tag)
    {
        broadcastManager.sendBroadcast(BroadcastUtil.failBroadcast(Type.META_TASK));
    }

    private class UpdatePackageListTask extends AsyncTask<InputStream, Void, Void>
    {
        @Override
        protected Void doInBackground(InputStream... params)
        {
            Log.i(TAG, "Update Package List Backgroupd");

            InputStream is = params[0];
            List<GTLanguage> languageList = GTPackageReader.processMetaResponse(is);

            Log.i(TAG, "List Size: " + languageList.size());

            adapter.open();

            for (GTLanguage gtl : languageList)
            {
                // check if language is already in the db
                GTLanguage dbLanguage = adapter.getGTLanguage(gtl.getLanguageCode());
                if (dbLanguage == null)
                {
                    adapter.insertGTLanguage(gtl);
                }
                else
                {
                    // don't forget that a previously downloaded language was already downloaded.
                    gtl.setDownloaded(dbLanguage.isDownloaded());
                    adapter.updateGTLanguage(gtl);
                }

                dbLanguage = adapter.getGTLanguage(gtl.getLanguageCode());
                for (GTPackage gtp : gtl.getPackages())
                {
                    // check if a new package is available for download or an existing package has been updated
                    GTPackage dbPackage = adapter.getGTPackage(gtp.getCode(), gtp.getLanguage(), gtp.getStatus());
                    if (dbPackage == null || (gtp.getVersion() > dbPackage.getVersion()))
                    {
                        dbLanguage.setDownloaded(false);
                    }
                }

                adapter.updateGTLanguage(dbLanguage);
            }

            GTLanguage gtlPrimary = adapter.getGTLanguage(languagePrimary);
            GTLanguage gtlParallel = adapter.getGTLanguage(languageParallel);

            if (isFirstLaunch())
            {
                if (shouldUpdateLanguageSettings())
                {
                    // download resources for the phone's language
                    String languagePhone = ((SnuffyApplication) getApplication()).getDeviceLocale().getLanguage();
                    BackgroundService.downloadLanguagePack(BackgroundService.this, languagePhone, KEY_NEW_LANGUAGE);
                }
                else if (!gtlPrimary.isDownloaded())
                {
                    BackgroundService.downloadLanguagePack(BackgroundService.this, languagePrimary, KEY_UPDATE_PRIMARY);
                }
            }
            else
            {
                Log.i(TAG, "Not First Launch");

                if (!gtlPrimary.isDownloaded())
                {
                    BackgroundService.downloadLanguagePack(BackgroundService.this, languagePrimary, KEY_UPDATE_PRIMARY);
                }
                else
                {
                    // update the resources for the parallel language
                    if (gtlParallel != null && !gtlParallel.isDownloaded())
                    {
                        BackgroundService.downloadLanguagePack(BackgroundService.this, gtlParallel.getLanguageCode(), KEY_UPDATE_PARALLEL);
                    }
                    else
                    {
                        broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.DOWNLOAD_TASK));
                    }
                }
            }
            adapter.close();

            return null;
        }
    }

    /**
     * Copies the english resources from assets to internal storage,
     * then saves package information on the database.
     */
    private class PrepareInitialContentTask extends AsyncTask<Void, Void, Void>
    {

        Context mContext;
        SnuffyApplication mApp;
        File documentsDir;

        public PrepareInitialContentTask(SnuffyApplication app)
        {
            mContext = app.getApplicationContext();
            documentsDir = app.getDocumentsDir();
            mApp = app;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            AssetManager manager = mContext.getAssets();

            File resourcesDir = new File(documentsDir, "resources");
            resourcesDir.mkdir();

            Log.i("resourceDir", resourcesDir.getAbsolutePath());

            try
            {
                // copy the files from assets/english to documents directory
                String[] files = manager.list("english");
                for (String fileName : files)
                {
                    InputStream is = manager.open("english/" + fileName);
                    File outFile = new File(resourcesDir, fileName);
                    OutputStream os = new FileOutputStream(outFile);

                    copyFile(is, os);
                    is.close();
                    is = null;
                    os.flush();
                    os.close();
                    os = null;
                }

                // meta.xml file contains the list of supported languages
                InputStream metaStream = manager.open("meta.xml");
                List<GTLanguage> languageList = GTPackageReader.processMetaResponse(metaStream);
                for (GTLanguage gtl : languageList)
                {
                    gtl.addToDatabase(mContext);
                }

                // contents.xml file contains information about the bundled english resources
                InputStream contentsStream = manager.open("contents.xml");
                List<GTPackage> packageList = GTPackageReader.processContentFile(contentsStream);
                for (GTPackage gtp : packageList)
                {
                    Log.i("addingDB", gtp.getName());
                    gtp.addToDatabase(mContext);
                }

                // Add Every Student to database
                GTPackage everyStudent = new GTPackage();
                everyStudent.setCode(GTPackage.EVERYSTUDENT_PACKAGE_CODE);
                everyStudent.setName("Every Student");
                everyStudent.setIcon("homescreen_everystudent_icon_2x.png");
                everyStudent.setStatus("live");
                everyStudent.setLanguage("en");
                everyStudent.setVersion(1.1);

                everyStudent.addToDatabase(mContext);

                // english resources should be marked as downloaded
                GTLanguage gtlEnglish = new GTLanguage("en");
                gtlEnglish.setDownloaded(true);
                gtlEnglish.update(mContext);

            } catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }

    private boolean shouldUpdateLanguageSettings()
    {
        // check first if the we support the phones language
        String languagePhone = ((SnuffyApplication) getApplication()).getDeviceLocale().getLanguage();
        GTLanguage gtlPhone = GTLanguage.getLanguage(this, languagePhone);
        return gtlPhone != null && !languagePrimary.equalsIgnoreCase(languagePhone);
    }

    private boolean isFirstLaunch()
    {
        return settings.getBoolean("firstLaunch", true);
    }
}
