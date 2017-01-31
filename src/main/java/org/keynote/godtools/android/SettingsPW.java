package org.keynote.godtools.android;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.apache.commons.lang3.text.WordUtils;
import org.ccci.gto.android.common.util.LocaleCompat;
import org.keynote.godtools.android.broadcast.BroadcastUtil;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.fragments.AccessCodeDialogFragment;
import org.keynote.godtools.android.fragments.ConfirmDialogFragment;
import org.keynote.godtools.android.googleAnalytics.EventTracker;
import org.keynote.godtools.android.service.BackgroundService;
import org.keynote.godtools.android.utils.Device;
import org.keynote.godtools.renderer.crureader.bo.GPage.Util.TypefaceUtils;

import java.util.Locale;

import static org.keynote.godtools.android.utils.Constants.AUTH_DRAFT;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.REGISTRATION_ID;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;

public class SettingsPW extends BaseActionBarActivity implements
        View.OnClickListener,
        ConfirmDialogFragment.OnConfirmClickListener,
        AccessCodeDialogFragment.AccessCodeDialogListener
{
    private static final String TAG = SettingsPW.class.getSimpleName();
    private static final int REQUEST_PRIMARY = 1002;
    private static final int REQUEST_PARALLEL = 1003;

    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;
    @NonNull
    private EventTracker mTracker;

    private TextView tvMainLanguage;
    private TextView tvParallelLanguage;
    private CompoundButton cbTranslatorMode;
    private CompoundButton cbNotificationsAllowed;

    private ProgressDialog pdLoading;

    private final Locale mDeviceLocale = Locale.getDefault();

    private SharedPreferences settings;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        mTracker = EventTracker.getInstance(this);

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
        RelativeLayout rlMainLanguage = (RelativeLayout) findViewById(R.id.rlMainLanguage);
        RelativeLayout rlParallelLanguage = (RelativeLayout) findViewById(R.id.rlParallelLanguage);
        cbTranslatorMode = (CompoundButton) findViewById(R.id.cbTranslatorMode);
        cbNotificationsAllowed = (CompoundButton) findViewById(R.id.cbNotification);

        // set click listeners
        rlParallelLanguage.setOnClickListener(this);
        rlMainLanguage.setOnClickListener(this);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isTranslatorEnabled = settings.getBoolean(TRANSLATOR_MODE, false);
        boolean allowNotifications = settings.getBoolean("Notifications", true);
        cbNotificationsAllowed.setChecked(allowNotifications);
        String primaryLanguageCode = settings.getString(GTLanguage.KEY_PRIMARY, ENGLISH_DEFAULT);
        String parallelLanguageCode = settings.getString(GTLanguage.KEY_PARALLEL, "");

        tvMainLanguage = TypefaceUtils.setTypeface(tvMainLanguage, primaryLanguageCode, Typeface.NORMAL);
        tvParallelLanguage = TypefaceUtils.setTypeface(tvParallelLanguage, primaryLanguageCode, Typeface.NORMAL);

        // set up translator switch
        cbTranslatorMode.setChecked(isTranslatorEnabled);

        // set value for primary language view
        Locale localePrimary = LocaleCompat.forLanguageTag(primaryLanguageCode);
        String primaryName = WordUtils.capitalize(localePrimary.getDisplayName(mDeviceLocale));
        tvMainLanguage.setText(primaryName);

        setupBroadcastReceiver();

        // set value for parallel language view
        if (Strings.isNullOrEmpty(parallelLanguageCode)) {
            tvParallelLanguage.setText(getString(R.string.none));
        }
        else
        {
            Locale localeParallel = LocaleCompat.forLanguageTag(parallelLanguageCode);
            String parallelName = WordUtils.capitalize(localeParallel.getDisplayName(mDeviceLocale));
            tvParallelLanguage.setText(parallelName);
        }

        mTracker.screenView(EventTracker.SCREEN_SETTINGS, primaryLanguageCode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTracker.activeScreen(EventTracker.SCREEN_SETTINGS);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        removeBroadcastReceiver();
    }

    /* END lifecycle */

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

                tvMainLanguage = TypefaceUtils.setTypeface(tvMainLanguage, languagePrimary, Typeface.BOLD);
                tvParallelLanguage = TypefaceUtils.setTypeface(tvParallelLanguage, languagePrimary, Typeface.BOLD);

                // set value for primary language view
                Locale localePrimary = LocaleCompat.forLanguageTag(languagePrimary);
                String primaryName = WordUtils.capitalize(localePrimary.getDisplayName(mDeviceLocale));
                tvMainLanguage.setText(primaryName);

                setParallelLanguageField(null);

                mTracker.settingChanged("Language Change", "Change Primary Language");
                break;
            }
            case RESULT_CHANGED_PARALLEL:
            {
                setParallelLanguageField(data.getStringExtra("parallelCode"));

                mTracker.settingChanged("Language Change", "Change Parallel Language");
                break;
            }
            case RESULT_DOWNLOAD_PRIMARY:
            case RESULT_DOWNLOAD_PARALLEL:
                break;
        }

    }

    private void setParallelLanguageField(String languageCode)
    {
        if (Strings.isNullOrEmpty(languageCode))
        {
            tvParallelLanguage.setText(getString(R.string.none));
        }
        else
        {
            Locale localeParallel = LocaleCompat.forLanguageTag(languageCode);
            String parallelName = WordUtils.capitalize(localeParallel.getDisplayName(mDeviceLocale));
            tvParallelLanguage.setText(parallelName);
        }
    }

    @Override
    public void onClick(View v)
    {

        Intent intent = new Intent(SettingsPW.this, SelectLanguagePW.class);

        switch (v.getId())
        {
            case R.id.rlMainLanguage:
                intent.putExtra("languageType", getString(R.string.settings_main_language));
                startActivityForResult(intent, REQUEST_PRIMARY);
                break;

            case R.id.rlParallelLanguage:
                intent.putExtra("languageType", getString(R.string.settings_parallel_language));
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

            return;
        }

        view.setEnabled(false);

        if (on)
        {
            if (Device.isConnected(SettingsPW.this))
            {
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

        mTracker.settingChanged("Notification State", event);

        updateDeviceWithAPI(cbNotificationsAllowed.isChecked());
    }

    private void showLoading(String msg)
    {
        pdLoading = new ProgressDialog(SettingsPW.this);
        pdLoading.setCancelable(false);
        pdLoading.setMessage(msg);
        pdLoading.show();

    }

    private void updateDeviceWithAPI(boolean notificationsOn)
    {
        String registrationId = settings.getString(REGISTRATION_ID, "");
        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        if (!Strings.isNullOrEmpty(registrationId) && !Strings.isNullOrEmpty(deviceId))
        {
            BackgroundService.registerDevice(getApplicationContext(), registrationId, deviceId, notificationsOn);
        }
    }
}
