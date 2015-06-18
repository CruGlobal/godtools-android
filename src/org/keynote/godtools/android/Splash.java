package org.keynote.godtools.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crittercism.app.Crittercism;

import org.keynote.godtools.android.broadcast.BroadcastUtil;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.service.BackgroundService;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import static org.keynote.godtools.android.utils.Constants.AUTH_CODE;
import static org.keynote.godtools.android.utils.Constants.EMPTY_STRING;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.FIRST_LAUNCH;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;


public class Splash extends Activity
{
    private static final String TAG = Splash.class.getSimpleName();

    protected boolean _active = true;
    private TextView tvTask;
    private ProgressBar progressBar;
    private SharedPreferences settings;
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

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

        setupBroadcastReceiver();

        Log.i(TAG, "First Launch");

        // set english as primary language on first start
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(GTLanguage.KEY_PRIMARY, ENGLISH_DEFAULT);
        editor.apply();

        // set up files
        BackgroundService.firstSetup((SnuffyApplication) getApplication());

        // if connected to the internet and not auth code (why would there be? It is
        // the first run.
        if (Device.isConnected(Splash.this) &&
                EMPTY_STRING.equals(settings.getString(AUTH_CODE, EMPTY_STRING)))
        {
            // get an auth code
            Log.i(TAG, "Starting backgound service");
            BackgroundService.authenticateGeneric(this);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        removeBroadcastReceiver();
    }

    private void setupBroadcastReceiver()
    {
        broadcastManager = LocalBroadcastManager.getInstance(this);

        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (BroadcastUtil.ACTION_STOP.equals(intent.getAction()))
                {
                    Log.i(TAG, "Action Done");

                    Type type = (Type) intent.getSerializableExtra(BroadcastUtil.ACTION_TYPE);

                    switch (type)
                    {
                        case AUTH:
                            // user is authorized. This is the first run

                            Log.i(TAG, "Auth Task complete");
                            // check for updates.
                            checkForUpdates();
                            break;
                        case DOWNLOAD_TASK:
                            goToMainActivity();
                            break;
                        case META_TASK:
                            Log.i(TAG, "Meta complete");
                            showLoading("Updating");
                            break;
                        case ERROR:
                            Log.i(TAG, "Error");
                            break;
                    }
                }

                if (BroadcastUtil.ACTION_FAIL.equals(intent.getAction()))
                {
                    Log.i(TAG, "Action Failed: " + intent.getSerializableExtra(BroadcastUtil.ACTION_TYPE));
                    goToMainActivity();
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtil.startFilter());
        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtil.stopFilter());
        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtil.failedFilter());
    }

    private void removeBroadcastReceiver()
    {
        broadcastManager.unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
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
        return settings.getBoolean(FIRST_LAUNCH, true);
    }

    private void showLoading(String msg)
    {
        tvTask.setText(msg);
        tvTask.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void checkForUpdates()
    {
        showLoading(getString(R.string.check_update));

        // This will be a meta task
        BackgroundService.getListOfPackages(this);
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
}