package org.keynote.godtools.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.service.PrepareInitialContentTask;
import org.keynote.godtools.android.service.UpdatePackageListTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.FIRST_LAUNCH;
import static org.keynote.godtools.android.utils.Constants.META;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;

/*
    Logic flow:

    If first load:
        - unpack bundled content
        - query API for latest available languages and packages
        - store latest languages and packages in local database
        - download initial content for device language, if available

    If not first load:
        - go to main activity (home screen)
 */
public class Splash extends Activity implements MetaTask.MetaTaskHandler, DownloadTask.DownloadTaskHandler {
    private static final String TAG = Splash.class.getSimpleName();

    @Bind(R.id.tvTask)
    TextView mUpdateText;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;

    private SharedPreferences settings;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_pw);
        ButterKnife.bind(this);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (!isFirstLaunch())
        {
            goToMainActivity();
        }
        else
        {
            Log.i(TAG, "First Launch");

            // set primary language on first start
            settings.edit().putString(GTLanguage.KEY_PRIMARY, Locale.getDefault().getLanguage()).apply();

            // set up files
            PrepareInitialContentTask.run(getApp().getApplicationContext(), getApp().getResourcesDir());

            showLoading();

            GodToolsApiClient.getListOfPackages(META,this);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        return true;
    }

    private boolean isFirstLaunch()
    {
        return settings.getBoolean(FIRST_LAUNCH, true);
    }

    private void showLoading() {
        mUpdateText.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void goToMainActivity()
    {
        // so now that we are expiring the translator code after 12 hours we will auto "log out" the
        // user when the app is restarted.

        if (settings.getBoolean(TRANSLATOR_MODE, false))
        {
            settings.edit().putBoolean(TRANSLATOR_MODE, false).apply();
        }

        Intent intent = new Intent(this, MainPW.class);
        startActivity(intent);
        finish();
    }

    private SnuffyApplication getApp()
    {
        return (SnuffyApplication) getApplication();
    }

    @Override
    public void metaTaskComplete(List<GTLanguage> languageList, String tag)
    {
        UpdatePackageListTask.run(languageList,DBAdapter.getInstance(this));

        // if the API has packages available for the device default language then download them
        // this is determined by going through the results of the meta download
        if(apiHasDeviceDefaultLanguage(languageList))
        {
            GodToolsApiClient.downloadLanguagePack(
                    getApp(),
                    Locale.getDefault().getLanguage(),
                    "primary",
                    this);
        }
        // if not, then switch back to English and download those latest resources
        else
        {
            settings.edit().putString(GTLanguage.KEY_PRIMARY, ENGLISH_DEFAULT).apply();

            GodToolsApiClient.downloadLanguagePack(
                    getApp(),
                    ENGLISH_DEFAULT,
                    "primary",
                    this);
        }

    }

    private boolean apiHasDeviceDefaultLanguage(List<GTLanguage> languageList)
    {
        for(GTLanguage metaLanguageFromInitialDownload : languageList)
        {
            if(metaLanguageFromInitialDownload.getLanguageCode().equalsIgnoreCase(Locale.getDefault().getLanguage()) &&
                    !metaLanguageFromInitialDownload.getPackages().isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void metaTaskFailure(List<GTLanguage> languageList, String tag, int statusCode)
    {
        goToMainActivity();
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag)
    {
        GTLanguage languageRetrievedFromDatabase = GTLanguage.getLanguage(this, langCode);
        languageRetrievedFromDatabase.setDownloaded(true);
        languageRetrievedFromDatabase.update(this);

        goToMainActivity();
    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
    {
        // if there was an error downloading resources, then switch the phone's language back to English since those are the
        // resources that were bundled.  user would get a blank screen if not
        settings.edit().putString(GTLanguage.KEY_PRIMARY, ENGLISH_DEFAULT).apply();
        goToMainActivity();
    }
}