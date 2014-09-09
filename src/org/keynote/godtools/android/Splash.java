package org.keynote.godtools.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crittercism.app.Crittercism;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;


public class Splash extends Activity implements DownloadTask.DownloadTaskHandler, MetaTask.MetaTaskHandler {

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
    String authorization;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_pw);
		Log.i("Matt", "Starting splash");

        tvTask = (TextView) findViewById(R.id.tvTask);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Enable crash reporting
        Crittercism.initialize(getApplicationContext(), getString(R.string.key_crittercism));

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "en");
		Log.i("matt", "Priamry language is " + languagePrimary);
        languageParallel = settings.getString(GTLanguage.KEY_PARALLEL, "");

        authorization = getString(R.string.key_authorization_generic);

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
            showLoading(getString(R.string.copy_files));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ;
            AssetManager manager = mContext.getAssets();

            File resourcesDir = new File(documentsDir, "resources");
            resourcesDir.mkdir();

            Log.i("resourceDir", resourcesDir.getAbsolutePath());

            try {
                // copy the files from assets/english to documents directory
                String[] files = manager.list("english");
                for (String fileName : files) {
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
                for (GTLanguage gtl : languageList) {
                    gtl.addToDatabase(mContext);
                }

                // contents.xml file contains information about the bundled english resources
                InputStream contentsStream = manager.open("contents.xml");
                List<GTPackage> packageList = GTPackageReader.processContentFile(contentsStream);
                for (GTPackage gtp : packageList) {
					Log.i("addingDB", gtp.getName());
                    gtp.addToDatabase(mContext);
                }

				// Add Every Student to database
				GTPackage everyStudent = new GTPackage();
				everyStudent.setCode("everystudent");
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
                    GTPackage dbPackage = mAdapter.getGTPackage(gtp.getCode(), gtp.getLanguage(), gtp.getStatus());
                    if (dbPackage == null || (gtp.getVersion() > dbPackage.getVersion())) {
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

            if (isFirst) {

                if (shouldUpdateLanguageSettings()) {
                    // download resources for the phone's language
                    languagePhone = ((SnuffyApplication) getApplication()).getDeviceLocale().getLanguage();
                    Locale mLocale = new Locale(languagePhone);
                    showLoading(String.format(getString(R.string.download_resources), mLocale.getDisplayName()));
                    GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                            languagePhone,
                            KEY_NEW_LANGUAGE,
                            authorization,
                            Splash.this);
                } else if (!gtlPrimary.isDownloaded()) {
                    // update resources for the primary language
                    showLoading(String.format(getString(R.string.update_resources), gtlPrimary.getLanguageName()));
                    GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                            languagePrimary,
                            KEY_UPDATE_PRIMARY,
                            authorization,
                            Splash.this);
                } else {
                    goToMainActivity();
                }

            } else {

                if (!gtlPrimary.isDownloaded()) {
                    // update resources for the primary language
                    showLoading(String.format(getString(R.string.update_resources), gtlPrimary.getLanguageName()));
                    GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                            languagePrimary,
                            KEY_UPDATE_PRIMARY,
                            authorization,
                            Splash.this);

                } else {
                    // update the resources for the parallel language
                    if (gtlParallel != null && !gtlParallel.isDownloaded()) {
                        showLoading(String.format(getString(R.string.update_resources), gtlParallel.getLanguageName()));
                        GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                                gtlParallel.getLanguageCode(),
                                KEY_UPDATE_PARALLEL,
                                authorization,
                                Splash.this);

                    } else {
                        goToMainActivity();
                    }

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
        showLoading(getString(R.string.check_update));
        GodToolsApiClient.getListOfPackages(authorization, "meta", Splash.this);
    }

    @Override
    public void metaTaskComplete(InputStream is, String langCode, String tag) {

        new UpdatePackageListTask().execute(is);

    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag) {

        hideLoading();

        if (tag.equalsIgnoreCase(KEY_NEW_LANGUAGE)) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, langCode);
            editor.commit();

            GTLanguage gtl = new GTLanguage(langCode);
            gtl.setDownloaded(true);
            gtl.update(Splash.this);

            SnuffyApplication app = (SnuffyApplication) getApplication();
            app.setAppLocale(langCode);

            goToMainActivity();

        } else if (tag.equalsIgnoreCase(KEY_UPDATE_PRIMARY)) {

            gtlPrimary.setDownloaded(true);
            gtlPrimary.update(Splash.this);

            if (gtlParallel != null && !gtlParallel.isDownloaded()) {
                showLoading(String.format(getString(R.string.update_resources), gtlParallel.getLanguageName()));
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        gtlParallel.getLanguageCode(),
                        KEY_UPDATE_PARALLEL,
                        getString(R.string.key_authorization_generic),
                        Splash.this);
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
    public void metaTaskFailure(InputStream is, String langCode, String tag) {

        goToMainActivity();

    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag) {
        // TODO: show dialog to inform the user that the download failed
        finish();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, Main.class);
        finish();
        startActivity(intent);
    }
}