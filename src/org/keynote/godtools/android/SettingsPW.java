package org.keynote.godtools.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.fragments.AccessCodeDialogFragment;
import org.keynote.godtools.android.fragments.ConfirmDialogFragment;
import org.keynote.godtools.android.http.AuthTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.snuffy.SnuffyAlternateTypefaceTextView;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;
import org.keynote.godtools.android.utils.LanguagesNotSupportedByDefaultFont;
import org.keynote.godtools.android.utils.Typefaces;

import java.util.Locale;

public class SettingsPW extends BaseActionBarActivity implements
        View.OnClickListener,
        ConfirmDialogFragment.OnConfirmClickListener,
        AccessCodeDialogFragment.AccessCodeDialogListener,
        AuthTask.AuthTaskHandler {

    private static final int REQUEST_PRIMARY = 1002;
    private static final int REQUEST_PARALLEL = 1003;

    TextView tvMainLanguage, tvParallelLanguage, tvAbout;
    RelativeLayout rlMainLanguage, rlParallelLanguage;
    CompoundButton cbTranslatorMode;
    Typeface mAlternateTypeface;
    String primaryLanguageCode;

    ProgressDialog pdLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        tvMainLanguage = (TextView) findViewById(R.id.tvMainLanguage);
        tvParallelLanguage = (TextView) findViewById(R.id.tvParallelLanguage);
        tvAbout = (TextView) findViewById(R.id.tvAbout);
        rlMainLanguage = (RelativeLayout) findViewById(R.id.rlMainLanguage);
        rlParallelLanguage = (RelativeLayout) findViewById(R.id.rlParallelLanguage);
        cbTranslatorMode = (CompoundButton) findViewById(R.id.cbTranslatorMode);

        // set click listeners
        rlParallelLanguage.setOnClickListener(this);
        rlMainLanguage.setOnClickListener(this);
        tvAbout.setOnClickListener(this);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isTranslatorEnabled = settings.getBoolean("TranslatorMode", false);
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

        // set value for parallel language view
        if (parallelLanguageCode.isEmpty()) {
            tvParallelLanguage.setText(getString(R.string.none));
        } else {
            Locale localeParallel = new Locale(parallelLanguageCode);
            String parallelName = capitalizeFirstLetter(localeParallel.getDisplayName());
            tvParallelLanguage.setText(parallelName);
        }

        trackScreenActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED)
            setResult(resultCode, data);

        switch (resultCode) {
            case RESULT_CHANGED_PRIMARY: {
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

                if (parallelLanguageCode.isEmpty()) {
                    tvParallelLanguage.setText(getString(R.string.none));
                } else {
                    Locale localeParallel = new Locale(parallelLanguageCode);
                    String parallelName = capitalizeFirstLetter(localeParallel.getDisplayName());
                    tvParallelLanguage.setText(parallelName);
                }

                trackScreenEvent("Change Primary Language");
                break;
            }
            case RESULT_CHANGED_PARALLEL: {

                // set value for parallel language view
                String languageParallel = data.getStringExtra("parallelCode");
                Locale localeParallel = new Locale(languageParallel);
                String parallelName = capitalizeFirstLetter(localeParallel.getDisplayName());
                tvParallelLanguage.setText(parallelName);

                trackScreenEvent("Change Parallel Language");
                break;
            }
            case RESULT_DOWNLOAD_PRIMARY:
            case RESULT_DOWNLOAD_PARALLEL:
                finish();
                break;
        }

    }

    @Override
    public void onClick(View v) {

        Intent intent = new Intent(SettingsPW.this, SelectLanguagePW.class);

        switch (v.getId()) {
            case R.id.rlMainLanguage:
                intent.putExtra("languageType", "Main Language");
                startActivityForResult(intent, REQUEST_PRIMARY);
                break;

            case R.id.rlParallelLanguage:
                intent.putExtra("languageType", "Parallel Language");
                startActivityForResult(intent, REQUEST_PARALLEL);
                break;

            case R.id.tvAbout:
                intent = new Intent(SettingsPW.this, About.class);
                startActivity(intent);
                break;
        }

    }

    public void onToggleClicked(View view) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean on = ((CompoundButton) view).isChecked();

        if(on && !settings.getString("Authorization_Draft", "").isEmpty())
        {
            ((CompoundButton) view).setChecked(true);
            cbTranslatorMode.setEnabled(true);
            setTranslatorMode(true);
            setResult(RESULT_PREVIEW_MODE_ENABLED);
            finish();
            return;
        }

        view.setEnabled(false);

        if (on) {

            if (Device.isConnected(SettingsPW.this))
                showAccessCodeDialog();
            else {
                Toast.makeText(SettingsPW.this, "Internet connection is needed to enable translator mode", Toast.LENGTH_SHORT).show();
                ((CompoundButton) view).setChecked(false);
                view.setEnabled(true);
            }


        } else {
            showExitTranslatorModeDialog();
        }
    }

    private void showAccessCodeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment frag = (DialogFragment) fm.findFragmentByTag("access_dialog");
        if (frag == null) {
            frag = new AccessCodeDialogFragment();
            frag.setCancelable(false);
            frag.show(fm, "access_dialog");
        }
    }


    private void showExitTranslatorModeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment frag = (DialogFragment) fm.findFragmentByTag("confirm_dialog");
        if (frag == null) {
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
    public void onConfirmClick(boolean positive, String tag) {

        if (tag.equalsIgnoreCase("ExitTranslatorMode")) {

            if (positive) {
                // disable translator mode
                setResult(RESULT_PREVIEW_MODE_DISABLED);
                setTranslatorMode(false);

                finish();

            } else {
                cbTranslatorMode.setChecked(true);
            }

            cbTranslatorMode.setEnabled(true);
        }

    }

    @Override
    public void onAccessDialogClick(boolean positive, String accessCode) {

        if (positive) {
            // start the authentication
            if (accessCode.isEmpty()) {
                Toast.makeText(SettingsPW.this, "Invalid Access Code", Toast.LENGTH_SHORT).show();
                cbTranslatorMode.setChecked(false);
                cbTranslatorMode.setEnabled(true);
            } else {
                showLoading("Authenticating access code");
                GodToolsApiClient.authenticateAccessCode(accessCode, this);
            }

        } else {
            cbTranslatorMode.setChecked(false);
        }

        cbTranslatorMode.setEnabled(true);
    }

    @Override
    public void authComplete(String authorization) {
        pdLoading.dismiss();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Authorization_Draft", authorization);
        editor.apply();

        setTranslatorMode(true);
        setResult(RESULT_PREVIEW_MODE_ENABLED);
        finish();
    }

    @Override
    public void authFailed() {
        pdLoading.dismiss();
        Toast.makeText(SettingsPW.this, "Invalid Access Code", Toast.LENGTH_SHORT).show();
        cbTranslatorMode.setChecked(false);
        cbTranslatorMode.setEnabled(true);
    }

    private void setTranslatorMode(boolean isEnabled) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("TranslatorMode", isEnabled);
        editor.apply();
    }

    private String capitalizeFirstLetter(String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    private void showLoading(String msg) {
        pdLoading = new ProgressDialog(SettingsPW.this);
        pdLoading.setCancelable(false);
        pdLoading.setMessage(msg);
        pdLoading.show();

    }

    private void handleLanguagesWithAlternateFonts(String mAppLanguage) {
        if (LanguagesNotSupportedByDefaultFont.contains(mAppLanguage)) {
            mAlternateTypeface = Typefaces.get(getApplication(), LanguagesNotSupportedByDefaultFont.getPathToAlternateFont(mAppLanguage));
        } else {
            mAlternateTypeface = Typeface.DEFAULT;
        }
    }

    private Tracker getGoogleAnalyticsTracker()
    {
        return ((SnuffyApplication)getApplication()).getTracker();
    }

    private void trackScreenEvent(String event)
    {
        Tracker tracker = getGoogleAnalyticsTracker();
        tracker.setScreenName("Settings");
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Language Change")
                .setAction(event)
                .setLabel(event)
                .build());
    }

    private void trackScreenActivity()
    {
        Tracker tracker = getGoogleAnalyticsTracker();
        tracker.setScreenName("Settings");
        tracker.send(new HitBuilders.AppViewBuilder()
                .setCustomDimension(1, "Settings")
                .setCustomDimension(2, primaryLanguageCode)
                .build());
    }
}
