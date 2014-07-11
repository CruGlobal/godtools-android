package org.keynote.godtools.android;

import android.content.Intent;
import android.content.SharedPreferences;
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
import org.keynote.godtools.android.fragments.AlertDialogFragment;

import java.util.Locale;

public class SettingsPW extends ActionBarActivity implements
        View.OnClickListener,
        AlertDialogFragment.OnDialogClickListener,
        AccessCodeDialogFragment.AccessCodeDialogListener {

    private static final String PREFS_NAME = "GodTools";

    private static final int REQUEST_PRIMARY = 1002;
    private static final int REQUEST_PARALLEL = 1003;
    public static final int RESULT_DOWNLOAD_PRIMARY = 2001;
    public static final int RESULT_DOWNLOAD_PARALLEL = 2002;
    public static final int RESULT_CHANGED_PRIMARY = 2003;

    TextView tvMainLanguage, tvParallelLanguage, tvAbout;
    RelativeLayout rlMainLanguage, rlParallelLanguage;
    CompoundButton cbTranslatorMode;

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
        String primaryLanguageCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        String parallelLanguageCode = settings.getString(GTLanguage.KEY_PARALLEL, "");

        // set up primary language views
        Locale localePrimary = new Locale(primaryLanguageCode);
        tvMainLanguage.setText(localePrimary.getDisplayName());

        // set up parallel language views
        if (parallelLanguageCode.isEmpty()) {
            tvParallelLanguage.setText("None");
        } else {
            Locale localeParallel = new Locale(parallelLanguageCode);
            tvParallelLanguage.setText(localeParallel.getDisplayName());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_CANCELED)
            setResult(resultCode, data);

        if (requestCode == REQUEST_PRIMARY && resultCode == RESULT_DOWNLOAD_PRIMARY) {
            finish();
        } else if (requestCode == REQUEST_PARALLEL && resultCode == RESULT_DOWNLOAD_PARALLEL) {
            finish();
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
        view.setEnabled(false);

        boolean on = ((CompoundButton) view).isChecked();
        if (on) {
            showAccessCodeDialog();
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
        DialogFragment frag = (DialogFragment) fm.findFragmentByTag("alert_dialog");
        if (frag == null) {
            frag = AlertDialogFragment.newInstance(
                    getString(R.string.dialog_translator_mode_title),
                    getString(R.string.dialog_translator_mode_body),
                    "ExitTranslatorMode"
            );
            frag.show(fm, "alert_dialog");
        }
    }

    @Override
    public void onDialogClick(boolean positive, String tag) {

        if (tag.equalsIgnoreCase("ExitTranslatorMode")) {

            if (positive) {
                // disable translator mode
                Toast.makeText(SettingsPW.this, "Translator mode disabled", Toast.LENGTH_SHORT).show();
                setTranslatorMode(false);

            } else {
                // undo the switch
                toggleTranslatorMode();
            }

            cbTranslatorMode.setEnabled(true);
        }

    }

    @Override
    public void onAccessDialogClick(boolean positive, String accessCode) {

        if (positive) {
            // start the authentication
            Toast.makeText(SettingsPW.this, "Translator mode enabled with access code " +  accessCode, Toast.LENGTH_LONG).show();
            setTranslatorMode(true);

        } else {
            // undo the switch
            toggleTranslatorMode();
        }

        cbTranslatorMode.setEnabled(true);
    }

    private void toggleTranslatorMode() {
        boolean on = cbTranslatorMode.isChecked();
        cbTranslatorMode.setChecked(!on);
    }

    private void setTranslatorMode(boolean isEnabled){
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("TranslatorMode", isEnabled);
        editor.commit();
    }
}
