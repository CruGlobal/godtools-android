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

import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class Splash extends Activity implements DownloadTask.DownloadTaskHandler {
    protected boolean _active = true;
    protected int _splashTime = 2000;
    private static final String PREFS_NAME = "GodTools";

    private String languagePhone;

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

        if (isFirstLaunch()) {

            new PrepareInitialContentTask((SnuffyApplication) getApplication()).execute((Void) null);

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
        boolean isFirst = settings.getBoolean("firstLaunch", true);
        if (isFirst) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstLaunch", false);
            editor.commit();
        }
        return isFirst;
    }

    private void showLoading() {
        tvTask.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        tvTask.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private boolean shouldUpdateLanguageSettings() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String langPrimary = settings.getString("languagePrimary", "en");
        String langPhone = Device.getDefaultLanguage();

        return !langPrimary.equalsIgnoreCase(langPhone);
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
            tvTask.setText("Copying files from assets...");
            showLoading();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            AssetManager manager = mContext.getAssets();

            try {
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

                InputStream contentsStream = manager.open("contents.xml");
                List<GTPackage> packageList = GTPackageReader.processContentFile(contentsStream);

                adapter.open();
                for (GTPackage gtp : packageList) {
                    adapter.insertGTPackage(gtp);
                }
                adapter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (Device.isConnected(mContext) && shouldUpdateLanguageSettings()) {

                languagePhone = Device.getDefaultLanguage();
                tvTask.setText(String.format("Downloading %s package...", languagePhone));
                GodToolsApiClient.downloadLanguagePack(mApp, languagePhone, "", Splash.this);

            } else {

                goToMainActivity();

            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String tag) {

        hideLoading();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("languagePrimary", languagePhone);
        editor.commit();

        goToMainActivity();

    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String tag) {
        // TODO: show dialog to inform the user that the download failed
        finish();
    }

    private void goToMainActivity(){
        Intent intent = new Intent(this, MainPW.class);
        finish();
        startActivity(intent);
    }
}