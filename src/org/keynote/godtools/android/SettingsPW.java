package org.keynote.godtools.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import java.lang.reflect.Type;
import java.util.Locale;

public class SettingsPW extends ActionBarActivity implements
        View.OnClickListener,
        ConfirmDialogFragment.OnConfirmClickListener,
        AccessCodeDialogFragment.AccessCodeDialogListener,
        AuthTask.AuthTaskHandler {

    private static final String PREFS_NAME = "GodTools";

    private static final int REQUEST_PRIMARY = 1002;
    private static final int REQUEST_PARALLEL = 1003;
    public static final int RESULT_DOWNLOAD_PRIMARY = 2001;
    public static final int RESULT_DOWNLOAD_PARALLEL = 2002;
    public static final int RESULT_CHANGED_PRIMARY = 2003;
    public static final int RESULT_PREVIEW_MODE_ENABLED = 1234;
    public static final int RESULT_PREVIEW_MODE_DISABLED = 2345;

    String primaryLanguageCode, parallelLanguageCode;

    TextView tvMainLanguage, tvParallelLanguage, tvAbout;
    RelativeLayout rlMainLanguage, rlParallelLanguage;
    CompoundButton cbTranslatorMode;
    Typeface mAlternateTypeface;

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
        cbTranslatorMode.setChecked(isTranslatorEnabled);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        primaryLanguageCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        parallelLanguageCode = settings.getString(GTLanguage.KEY_PARALLEL, "");

        handleLanguagesWithAlternateFonts(primaryLanguageCode);
        tvMainLanguage = new SnuffyAlternateTypefaceTextView(tvMainLanguage).setAlternateTypeface(mAlternateTypeface, Typeface.BOLD).get();
        tvParallelLanguage = new SnuffyAlternateTypefaceTextView(tvParallelLanguage).setAlternateTypeface(mAlternateTypeface, Typeface.BOLD).get();

        // set up primary language views
        Locale localePrimary = new Locale(primaryLanguageCode);
        String primaryName = capitalizeFirstLetter(localePrimary.getDisplayName());
        tvMainLanguage.setText(primaryName);

        // set up parallel language views
        if (parallelLanguageCode.isEmpty()) {
            tvParallelLanguage.setText(getString(R.string.none));
        } else {
            Locale localeParallel = new Locale(parallelLanguageCode);
            String parallelName = capitalizeFirstLetter(localeParallel.getDisplayName());
            tvParallelLanguage.setText(parallelName);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED)
            setResult(resultCode, data);

        switch (resultCode) {
            case RESULT_CHANGED_PRIMARY: {
                SnuffyApplication app = (SnuffyApplication) getApplication();
                app.setAppLocale(data.getStringExtra("primaryCode"));
                break;
            }
            case RESULT_DOWNLOAD_PRIMARY:
            case RESULT_DOWNLOAD_PARALLEL:
                finish();
                break;
        }

        /**
        if (requestCode == REQUEST_PRIMARY && resultCode == RESULT_DOWNLOAD_PRIMARY) {
            finish();
        } else if (requestCode == REQUEST_PARALLEL && resultCode == RESULT_DOWNLOAD_PARALLEL) {
            finish();
        }
        */

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
        view.setEnabled(false);

        boolean on = ((CompoundButton) view).isChecked();
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
        editor.putString("authorization", authorization);
        editor.commit();

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
        editor.commit();
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
}
