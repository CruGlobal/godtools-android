package org.keynote.godtools.android;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.keynote.godtools.android.broadcast.BroadcastUtil;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.fragments.AccessCodeDialogFragment;
import org.keynote.godtools.android.fragments.ConfirmDialogFragment;
import org.keynote.godtools.android.googleAnalytics.EventTracker;
import org.keynote.godtools.android.service.BackgroundService;
import org.keynote.godtools.android.snuffy.SnuffyAlternateTypefaceTextView;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;
import org.keynote.godtools.android.utils.LanguagesNotSupportedByDefaultFont;
import org.keynote.godtools.android.utils.Typefaces;

import java.util.Locale;

import static org.keynote.godtools.android.utils.Constants.AUTH_DRAFT;
import static org.keynote.godtools.android.utils.Constants.REGISTRATION_ID;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.RESULT_CHANGED_PRIMARY;
import static org.keynote.godtools.android.utils.Constants.RESULT_CHANGED_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.RESULT_DOWNLOAD_PRIMARY;
import static org.keynote.godtools.android.utils.Constants.RESULT_DOWNLOAD_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.RESULT_PREVIEW_MODE_DISABLED;


public class SettingsPW extends ActionBarActivity implements
        View.OnClickListener,
        ConfirmDialogFragment.OnConfirmClickListener,
        AccessCodeDialogFragment.AccessCodeDialogListener
{

    private static final String TAG = SettingsPW.class.getSimpleName();
    private static final int REQUEST_PRIMARY = 1002;
    private static final int REQUEST_PARALLEL = 1003;

    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    TextView tvMainLanguage, tvParallelLanguage;
    RelativeLayout rlMainLanguage, rlParallelLanguage;
    CompoundButton cbTranslatorMode;
    CompoundButton cbNotificationsAllowed;
    Typeface mAlternateTypeface;
    String primaryLanguageCode;

    ProgressDialog pdLoading;

    SharedPreferences settings;

    // since the authFail method is used for either the wrong pass code or an expired pass code, we
    // need separate messages for each situation. If translatorModeExpired=false, then use wrong pass
    // code message. If true use expired message.
    boolean translatorModeExpired = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar_centered_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        TextView titleBar = (TextView) actionBar.getCustomView().findViewById(R.id.titlebar_title);
        titleBar.setText(R.string.settings_title);

        actionBar.setTitle(R.string.settings_title);
        actionBar.setDisplayShowTitleEnabled(true);

        tvMainLanguage = (TextView) findViewById(R.id.tvMainLanguage);
        tvParallelLanguage = (TextView) findViewById(R.id.tvParallelLanguage);
        rlMainLanguage = (RelativeLayout) findViewById(R.id.rlMainLanguage);
        rlParallelLanguage = (RelativeLayout) findViewById(R.id.rlParallelLanguage);
        cbTranslatorMode = (CompoundButton) findViewById(R.id.cbTranslatorMode);
        cbNotificationsAllowed = (CompoundButton) findViewById(R.id.cbNotification);

        // set click listeners
        rlParallelLanguage.setOnClickListener(this);
        rlMainLanguage.setOnClickListener(this);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isTranslatorEnabled = settings.getBoolean("TranslatorMode", false);
        boolean allowNotifications = settings.getBoolean("Notifications", true);
        cbNotificationsAllowed.setChecked(allowNotifications);
        primaryLanguageCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        String parallelLanguageCode = settings.getString(GTLanguage.KEY_PARALLEL, "");

        handleLanguagesWithAlternateFonts(primaryLanguageCode);
        tvMainLanguage = new SnuffyAlternateTypefaceTextView(tvMainLanguage).setAlternateTypeface(mAlternateTypeface, Typeface.NORMAL).get();
        tvParallelLanguage = new SnuffyAlternateTypefaceTextView(tvParallelLanguage).setAlternateTypeface(mAlternateTypeface, Typeface.NORMAL).get();

        // set up translator switch
        cbTranslatorMode.setChecked(isTranslatorEnabled);

        // set value for primary language view
        Locale localePrimary = new Locale(primaryLanguageCode);
        String primaryName = capitalizeFirstLetter(localePrimary.getDisplayName());
        tvMainLanguage.setText(primaryName);

        setupBroadcastReceiver();

        // set value for parallel language view
        if (Strings.isNullOrEmpty(parallelLanguageCode)) {
            tvParallelLanguage.setText(getString(R.string.none));
        }
        else
        {
            Locale localeParallel = new Locale(parallelLanguageCode);
            String parallelName = capitalizeFirstLetter(localeParallel.getDisplayName());
            tvParallelLanguage.setText(parallelName);
        }

        EventTracker.track(getApp(), "Settings", primaryLanguageCode);
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
                            break;
                        case ENABLE_TRANSLATOR:
                            // this would mean that the access code has been verified, go to preview mode

                            if (pdLoading != null) pdLoading.dismiss();
                            settings.edit().putBoolean(TRANSLATOR_MODE, true).apply();

                            startActivity(new Intent(SettingsPW.this, PreviewModeMainPW.class));
                            finish();
                            break;
                        case DISABLE_TRANSLATOR:
                            settings.edit().putBoolean(TRANSLATOR_MODE, false).apply();
                        case ERROR:
                            Log.i(TAG, "Error");
                            break;
                    }
                }

                if (BroadcastUtil.ACTION_FAIL.equals(intent.getAction()))
                {
                    Log.i(TAG, "Action Failed: " + intent.getSerializableExtra(BroadcastUtil.ACTION_TYPE));
                    cbTranslatorMode.setEnabled(true);
                    onToggleClicked(findViewById(R.id.cbTranslatorMode));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED)
            setResult(resultCode, data);

        switch (resultCode)
        {
            case RESULT_CHANGED_PRIMARY:
            {
                String languagePrimary = data.getStringExtra("primaryCode");
                SnuffyApplication app = (SnuffyApplication) getApplication();
                app.setAppLocale(languagePrimary);

                handleLanguagesWithAlternateFonts(languagePrimary);
                tvMainLanguage = new SnuffyAlternateTypefaceTextView(tvMainLanguage).setAlternateTypeface(mAlternateTypeface, Typeface.BOLD).get();
                tvParallelLanguage = new SnuffyAlternateTypefaceTextView(tvParallelLanguage).setAlternateTypeface(mAlternateTypeface, Typeface.BOLD).get();

                // set value for primary language view
                Locale localePrimary = new Locale(languagePrimary);
                String primaryName = capitalizeFirstLetter(localePrimary.getDisplayName());
                tvMainLanguage.setText(primaryName);

                // set value for parallel language view
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String parallelLanguageCode = settings.getString(GTLanguage.KEY_PARALLEL, "");

                if (Strings.isNullOrEmpty(parallelLanguageCode))
                {
                    tvParallelLanguage.setText(getString(R.string.none));
                }
                else
                {
                    Locale localeParallel = new Locale(parallelLanguageCode);
                    String parallelName = capitalizeFirstLetter(localeParallel.getDisplayName());
                    tvParallelLanguage.setText(parallelName);
                }

                EventTracker.track(getApp(), "Settings", "Language Change",
                        "Change Primary Language");
                break;
            }
            case RESULT_CHANGED_PARALLEL:
            {

                // set value for parallel language view
                String languageParallel = data.getStringExtra("parallelCode");
                Locale localeParallel = new Locale(languageParallel);
                String parallelName = capitalizeFirstLetter(localeParallel.getDisplayName());
                tvParallelLanguage.setText(parallelName);

                EventTracker.track(getApp(), "Settings",
                        "Language Change", "Change Parallel Language");
                break;
            }
            case RESULT_DOWNLOAD_PRIMARY:
            case RESULT_DOWNLOAD_PARALLEL:
                break;
        }

    }

    @Override
    public void onClick(View v)
    {

        Intent intent = new Intent(SettingsPW.this, SelectLanguagePW.class);

        switch (v.getId())
        {
            case R.id.rlMainLanguage:
                intent.putExtra("languageType", "Main Language");
                startActivityForResult(intent, REQUEST_PRIMARY);
                break;

            case R.id.rlParallelLanguage:
                intent.putExtra("languageType", "Parallel Language");
                startActivityForResult(intent, REQUEST_PARALLEL);
                break;
        }

    }

    public void onToggleClicked(View view)
    {
        boolean on = ((CompoundButton) view).isChecked();

        if (on && !Strings.isNullOrEmpty(settings.getString(AUTH_DRAFT, "")))
        {
            ((CompoundButton) view).setChecked(true);
            cbTranslatorMode.setEnabled(true);
            BackgroundService.verifyStatusOfAuthToken(getApplicationContext(),
                    settings.getString(AUTH_DRAFT, ""));

            // if Auth fails from this it is because the auth token is expired.
            translatorModeExpired = true;

            return;
        }

        view.setEnabled(false);

        if (on)
        {
            if (Device.isConnected(SettingsPW.this))
            {
                // if auth fails here it is because of the wrong pass code.
                translatorModeExpired = false;
                showAccessCodeDialog();
            }
            else
            {
                Toast.makeText(SettingsPW.this, getString(R.string.internet_needed), Toast.LENGTH_SHORT).show();
                ((CompoundButton) view).setChecked(false);
                view.setEnabled(true);
            }
        }
        else
        {
            showExitTranslatorModeDialog();
        }
    }

    private void showAccessCodeDialog()
    {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment frag = (DialogFragment) fm.findFragmentByTag("access_dialog");
        if (frag == null)
        {
            frag = new AccessCodeDialogFragment();
            frag.setCancelable(false);
            frag.show(fm, "access_dialog");
        }
    }


    private void showExitTranslatorModeDialog()
    {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment frag = (DialogFragment) fm.findFragmentByTag("confirm_dialog");
        if (frag == null)
        {
            frag = ConfirmDialogFragment.newInstance(
                    getString(R.string.dialog_translator_mode_title),
                    getString(R.string.dialog_translator_mode_body),
                    getString(R.string.yes),
                    getString(R.string.no),
                    "ExitTranslatorMode"
            );
            frag.show(fm, "confirm_dialog");
        }
    }

    @Override
    public void onConfirmClick(boolean positive, String tag)
    {

        if (tag.equalsIgnoreCase("ExitTranslatorMode"))
        {

            if (positive)
            {
                // disable translator mode
                setResult(RESULT_PREVIEW_MODE_DISABLED);
                LocalBroadcastManager.getInstance(this).sendBroadcast(BroadcastUtil.stopBroadcast(Type.DISABLE_TRANSLATOR));
                Intent intent = new Intent(this, MainPW.class);
                startActivity(intent);
                finish();

            }
            else
            {
                cbTranslatorMode.setChecked(true);
            }

            cbTranslatorMode.setEnabled(true);
        }

    }

    @Override
    public void onAccessDialogClick(boolean success)
    {

        if (!success)
        {
            if (pdLoading != null) pdLoading.dismiss();

            cbTranslatorMode.setChecked(false);
            cbTranslatorMode.setEnabled(true);
        }
        else
        {
            showLoading(getString(R.string.authenticate_code));
            cbTranslatorMode.setChecked(true);
            cbTranslatorMode.setEnabled(false);
        }
    }

    public void onNotificationToggle(View view)
    {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("Notifications", cbNotificationsAllowed.isChecked());
        editor.apply();

        String event = cbNotificationsAllowed.isChecked() ? "Turned ON" : "Turned OFF";

        EventTracker.track(getApp(), "Settings", "Notification State", event);

        String notificationsOn = cbNotificationsAllowed.isChecked() ? "TRUE" : "FALSE";
        updateDeviceWithAPI(notificationsOn);
    }

    private String capitalizeFirstLetter(String word)
    {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    private void showLoading(String msg)
    {
        pdLoading = new ProgressDialog(SettingsPW.this);
        pdLoading.setCancelable(false);
        pdLoading.setMessage(msg);
        pdLoading.show();

    }

    private void handleLanguagesWithAlternateFonts(String mAppLanguage)
    {
        if (LanguagesNotSupportedByDefaultFont.contains(mAppLanguage))
        {
            mAlternateTypeface = Typefaces.get(getApplication(), LanguagesNotSupportedByDefaultFont.getPathToAlternateFont(mAppLanguage));
        }
        else
        {
            mAlternateTypeface = Typeface.DEFAULT;
        }
    }

    private void updateDeviceWithAPI(String notificationsOn)
    {
        String registrationId = settings.getString(REGISTRATION_ID, "");
        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        if (!Strings.isNullOrEmpty(registrationId) && !Strings.isNullOrEmpty(deviceId))
        {
            BackgroundService.registerDevice(getApplicationContext(), registrationId, deviceId, notificationsOn);
        }
    }

    private SnuffyApplication getApp()
    {
        return (SnuffyApplication) getApplication();
    }
}
