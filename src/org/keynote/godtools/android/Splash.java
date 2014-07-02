package org.keynote.godtools.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.HttpTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;


public class Splash extends Activity implements DownloadTask.DownloadTaskHandler, HttpTask.HttpTaskHandler {

    private static final String LOG_TAG = "splash";

    protected boolean _active = true;
    protected int _splashTime = 2000;
    private static final String PREFS_NAME = "GodTools";
    private static final String KEY_NEW_LANGUAGE = "new_language";
    private static final String KEY_UPDATE_PRIMARY = "update_primary";
    private static final String KEY_UPDATE_PARALLEL = "update_parallel";

    private String languagePhone;
    private String languagePrimary;
    private String languageParallel;
    private boolean isFirst;

    private GTLanguage gtlPrimary;
    private GTLanguage gtlParallel;

    TextView tvTask;
    ProgressBar progressBar;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_pw);
        tvTask = (TextView) findViewById(R.id.tvTask);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Enable crash reporting
        //Crittercism.initialize(getApplicationContext(), getString(R.string.key_crittercism));

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        languageParallel = settings.getString(GTLanguage.KEY_PARALLEL, "");
        languagePhone = Device.getDefaultLanguage();

        if (isFirstLaunch()) {
            new PrepareInitialContentTask((SnuffyApplication) getApplication()).execute((Void) null);

        } else if (Device.isConnected(Splash.this)) {
            checkForUpdates();

        } else {
            // thread for displaying the SplashScreen
            Thread splashThread = new Thread() {
                @Override
                public void run() {
                    try {
                        int waited = 0;
                        while (_active && (waited < _splashTime)) {
                            sleep(100);
                            if (_active) {
                                waited += 100;
                            }
                        }
                    } catch (InterruptedException e) {
                        // do nothing
                    } finally {

                        goToMainActivity();

                    }
                }
            };
            splashThread.start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        _active = false;
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        _active = false;
        return true;
    }

    private boolean isFirstLaunch() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isFirst = settings.getBoolean("firstLaunch", true);
        if (isFirst) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstLaunch", false);
            editor.commit();
        }
        return isFirst;
    }

    private void showLoading(String msg) {
        tvTask.setText(msg);
        tvTask.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        tvTask.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private boolean shouldUpdateLanguageSettings() {

        // check first if the we support the phones language
        GTLanguage gtlPhone = GTLanguage.getLanguage(this, languagePhone);
        if (gtlPhone == null)
            return false;

        return !languagePrimary.equalsIgnoreCase(languagePhone);
    }

    /**
     * Copies the english resources from assets to internal storage,
     * then saves package information on the database.
     */
    private class PrepareInitialContentTask extends AsyncTask<Void, Void, Void> {

        Context mContext;
        SnuffyApplication mApp;
        File documentsDir;
        DBAdapter adapter;

        public PrepareInitialContentTask(SnuffyApplication app) {
            mContext = app.getApplicationContext();
            documentsDir = app.getDocumentsDir();
            adapter = DBAdapter.getInstance(mContext);
            mApp = app;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading("Copying files from assets...");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AssetManager manager = mContext.getAssets();

            try {
                // copy the files from assets/english to documents directory
                String[] files = manager.list("english");
                for (String fileName : files) {
                    InputStream is = manager.open("english/" + fileName);
                    File outFile = new File(documentsDir, fileName);
                    OutputStream os = new FileOutputStream(outFile);

                    copyFile(is, os);
                    is.close();
                    is = null;
                    os.flush();
                    os.close();
                    os = null;
                }

                // meta.xml file contains information about the packages available on the server
                InputStream metaStream = manager.open("meta.xml");
                List<GTLanguage> languageList = GTPackageReader.processMetaResponse(metaStream);
                for (GTLanguage gtl : languageList) {
                    gtl.addToDatabase(mContext);
                    for (GTPackage gtp : gtl.getPackages()) {
                        gtp.addToDatabase(mContext);
                    }
                }

                // contents.xml file contains information about the bundled english resources
                InputStream contentsStream = manager.open("contents.xml");
                List<GTPackage> packageList = GTPackageReader.processContentFile(contentsStream);
                for (GTPackage gtp : packageList) {
                    gtp.update(mContext);
                }

                // english resources should be marked as downloaded
                GTLanguage gtlEnglish = new GTLanguage("en");
                gtlEnglish.setDownloaded(true);
                gtlEnglish.update(Splash.this);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (Device.isConnected(mContext)) {
                checkForUpdates();
            } else {
                goToMainActivity();
            }
        }
    }

    private class UpdatePackageListTask extends AsyncTask<InputStream, Void, Void> {
        DBAdapter mAdapter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mAdapter = DBAdapter.getInstance(Splash.this);
        }

        @Override
        protected Void doInBackground(InputStream... params) {
            InputStream is = params[0];
            // update the database
            List<GTLanguage> languageList = GTPackageReader.processMetaResponse(is);
            mAdapter.open();
            for (GTLanguage gtl : languageList) {

                // check if language is already in the db
                GTLanguage dbLanguage = mAdapter.getGTLanguage(gtl.getLanguageCode());
                if (dbLanguage == null)
                    mAdapter.insertGTLanguage(gtl);

                dbLanguage = mAdapter.getGTLanguage(gtl.getLanguageCode());
                for (GTPackage gtp : gtl.getPackages()) {

                    // check if a new package is available for download or an existing package has been updated
                    GTPackage dbPackage = mAdapter.getGTPackage(gtp.getCode(), gtp.getLanguage());
                    if (dbPackage == null) {
                        mAdapter.insertGTPackage(gtp);
                        dbLanguage.setDownloaded(false);
                    } else if (gtp.getVersion() > dbPackage.getVersion()) {
                        mAdapter.updateGTPackage(gtp);
                        dbLanguage.setDownloaded(false);
                    }
                }

                mAdapter.updateGTLanguage(dbLanguage);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            gtlPrimary = mAdapter.getGTLanguage(languagePrimary);
            gtlParallel = mAdapter.getGTLanguage(languageParallel);

            if (isFirst && shouldUpdateLanguageSettings()) {
                // download resources for the phones language
                Locale mLocale = new Locale(languagePhone);
                showLoading(String.format("Downloading %s resources...", mLocale.getDisplayName()));
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(), languagePhone, KEY_NEW_LANGUAGE, Splash.this);

            } else {

                if (gtlPrimary.isDownloaded()) {
                    if (gtlParallel != null && !gtlParallel.isDownloaded()) {
                        showLoading(String.format("Updating %s resources...", gtlParallel.getLanguageName()));
                        GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(), gtlParallel.getLanguageCode(), KEY_UPDATE_PARALLEL, Splash.this);

                    } else {
                        goToMainActivity();
                    }

                } else {
                    showLoading(String.format("Updating %s resources...", gtlPrimary.getLanguageName()));
                    GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(), languagePhone, KEY_UPDATE_PRIMARY, Splash.this);
                }
            }

            mAdapter.close();
        }
    }


    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void checkForUpdates() {
        showLoading("Checking for updates...");
        GodToolsApiClient.getListOfPackages("", Splash.this);
    }

    @Override
    public void httpTaskComplete(String url, InputStream is, int statusCode, String tag) {

        new UpdatePackageListTask().execute(is);

    }

    @Override
    public void httpTaskFailure(String url, InputStream is, int statusCode, String tag) {
        // Toast.makeText(Splash.this, "Failed to update resources", Toast.LENGTH_SHORT).show();
        goToMainActivity();
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String tag) {

        hideLoading();

        if (tag.equalsIgnoreCase(KEY_NEW_LANGUAGE)) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, languagePhone);
            editor.commit();

            GTLanguage gtl = new GTLanguage(languagePhone);
            gtl.setDownloaded(true);
            gtl.update(Splash.this);

        } else if (tag.equalsIgnoreCase(KEY_UPDATE_PRIMARY)) {

            gtlPrimary.setDownloaded(true);
            gtlPrimary.update(Splash.this);

            if (gtlParallel != null && !gtlParallel.isDownloaded()) {
                showLoading(String.format("Updating %s resources...", gtlParallel.getLanguageName()));
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(), gtlParallel.getLanguageCode(), KEY_UPDATE_PARALLEL, Splash.this);
            } else {
                goToMainActivity();
            }


        } else if (tag.equalsIgnoreCase(KEY_UPDATE_PARALLEL)) {

            gtlParallel.setDownloaded(true);
            gtlParallel.update(Splash.this);

            goToMainActivity();
        }



    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String tag) {
        // TODO: show dialog to inform the user that the download failed
        finish();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainPW.class);
        finish();
        startActivity(intent);
    }
}