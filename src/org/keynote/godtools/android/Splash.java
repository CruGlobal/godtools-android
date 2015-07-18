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

import com.crittercism.app.Crittercism;
import com.google.common.base.Strings;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.service.PrepareInitialContentTask;
import org.keynote.godtools.android.service.UpdatePackageListTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.util.List;

import static org.keynote.godtools.android.utils.Constants.LANG_CODE;
import static org.keynote.godtools.android.utils.Constants.META;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;

/*
    Logic flow:

    If first load:
        - unpack bundled content
        - query API for latest available languages and packages
        - store latest languages and packages in local database

    If not first load:
        - go to main activity (home screen)
 */
public class Splash extends Activity implements MetaTask.MetaTaskHandler
{
    private static final String TAG = Splash.class.getSimpleName();

    protected boolean _active = true;

    TextView tvTask;
    ProgressBar progressBar;
    SharedPreferences settings;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (!isFirstLaunch())
        {
            goToMainActivity();
        }

		// Enable crash reporting
		Crittercism.initialize(getApplicationContext(), getString(R.string.key_crittercism));

        setContentView(R.layout.splash_pw);

        tvTask = (TextView) findViewById(R.id.tvTask);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Log.i(TAG, "First Launch");

        // get the default language of the device os
        String deviceDefaultLanguage = Device.getDefaultLanguage(getApp());
        // set to english in case nothing is found.
        if (Strings.isNullOrEmpty(deviceDefaultLanguage)) deviceDefaultLanguage = "en";

        Log.i(TAG, deviceDefaultLanguage);

        // set primary language on first start
        settings.edit().putString(GTLanguage.KEY_PRIMARY, deviceDefaultLanguage).apply();

        // set up files
        PrepareInitialContentTask.run(getApp().getApplicationContext(), getApp().getDocumentsDir());

        showLoading(getString(R.string.check_update));

        GodToolsApiClient.getListOfPackages(META,this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        _active = false;
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
    {
        _active = false;
        return true;
    }

    private boolean isFirstLaunch()
    {
        return settings.getBoolean("firstLaunch", true);
    }

    private void showLoading(String msg)
    {
        tvTask.setText(msg);
        tvTask.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void goToMainActivity()
    {
        // so now that we are expiring the translator code after 12 hours we will auto "log out" the
        // user when the app is restarted.

        if (settings.getBoolean("TranslatorMode", false))
        {
            settings.edit().putBoolean("TranslatorMode", false).apply();
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
        UpdatePackageListTask.run(languageList,
                DBAdapter.getInstance(this),
                isFirstLaunch(),
                getApp(),
                settings.getString(LANG_CODE, ""),
                "",
                this);

        goToMainActivity();
    }

    @Override
    public void metaTaskFailure(List<GTLanguage> languageList, String tag, int statusCode)
    {
        goToMainActivity();
    }
}