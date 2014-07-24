package org.keynote.godtools.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.fragments.AccessCodeDialogFragment;
import org.keynote.godtools.android.fragments.AlertDialogFragment;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.HttpTask;
import org.keynote.godtools.android.utils.Device;

import java.io.InputStream;
import java.util.Locale;

public class SettingsPW extends ActionBarActivity implements
        View.OnClickListener,
        AlertDialogFragment.OnDialogClickListener,
        AccessCodeDialogFragment.AccessCodeDialogListener,
        HttpTask.HttpTaskHandler {

    private static final String PREFS_NAME = "GodTools";

    private static final int REQUEST_PRIMARY = 1002;
    private static final int REQUEST_PARALLEL = 1003;
    public static final int RESULT_DOWNLOAD_PRIMARY = 2001;
    public static final int RESULT_DOWNLOAD_PARALLEL = 2002;
    public static final int RESULT_CHANGED_PRIMARY = 2003;

    String primaryLanguageCode, parallelLanguageCode;

    TextView tvMainLanguage, tvParallelLanguage, tvAbout;
    RelativeLayout rlMainLanguage, rlParallelLanguage;
    CompoundButton cbTranslatorMode;

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

        // set up primary language views
        Locale localePrimary = new Locale(primaryLanguageCode);
        String primaryName = capitalizeFirstLetter(localePrimary.getDisplayName());
        tvMainLanguage.setText(primaryName);

        // set up parallel language views
        if (parallelLanguageCode.isEmpty()) {
            tvParallelLanguage.setText("None");
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

            if (Device.isConnected(SettingsPW.this))
                showAccessCodeDialog();
            else {
                Toast.makeText(SettingsPW.this, "Internet connection is needed to enable translator mode", Toast.LENGTH_SHORT).show();
                toggleTranslatorMode();
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
            //authenticateAccessCode(accessCode);
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

    private void authenticateAccessCode(String accessCode) {
        String authorization = getString(R.string.key_authorization_generic);
        showLoading("Authenticating access code");
        GodToolsApiClient.getTranslatorToken(authorization, accessCode, "Auth", this);
    }

    @Override
    public void httpTaskComplete(String url, InputStream is, int statusCode, String tag) {

        /**
         if (tag.equalsIgnoreCase("Auth")) {
         new AuthTask().execute(is);
         }
         else if (tag.equalsIgnoreCase("CheckForUpdates")) {
         new UpdatePackageListTask().execute(is);
         }
         */
    }

    @Override
    public void httpTaskFailure(String url, InputStream is, int statusCode, String tag) {
        setTranslatorMode(false);
        toggleTranslatorMode();
        pdLoading.dismiss();

        if (tag.equalsIgnoreCase("Auth")) {
            Toast.makeText(SettingsPW.this, "Authentication failed", Toast.LENGTH_SHORT).show();
        } else if (tag.equalsIgnoreCase("CheckForUpdates")) {
            Toast.makeText(SettingsPW.this, "Failed to update resources", Toast.LENGTH_SHORT).show();
        }
    }

    private class AuthTask extends AsyncTask<InputStream, Void, String> {

        @Override
        protected String doInBackground(InputStream... params) {
            InputStream is = params[0];

            String authorization = GTPackageReader.processAuthResponse(is);

            if (authorization != null) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("authorization", authorization);
                editor.commit();
            }

            return authorization;
        }

        @Override
        protected void onPostExecute(String authorization) {
            super.onPostExecute(authorization);
            pdLoading.dismiss();

            Toast.makeText(SettingsPW.this, "Translator mode enabled", Toast.LENGTH_SHORT).show();

            /**
             pdLoading.setMessage("Checking for update...");
             GodToolsApiClient.getListOfPackages(authorization, "CheckForUpdates", SettingsPW.this);
             */
        }
    }
    /**
     private class UpdatePackageListTask extends AsyncTask<InputStream, Void, Void> {

     DBAdapter mAdapter;

     @Override protected void onPreExecute() {
     super.onPreExecute();
     pdLoading.setMessage("Updating resources...");
     mAdapter = DBAdapter.getInstance(SettingsPW.this);
     }

     @Override protected Void doInBackground(InputStream... params) {
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
     GTPackage dbPackage = mAdapter.getGTPackage(gtp.getCode(), gtp.getLanguage());
     if (dbPackage == null) {
     mAdapter.insertGTPackage(gtp);
     dbLanguage.setDownloaded(false);
     } else if (gtp.getVersion() > dbPackage.getVersion()) {
     mAdapter.updateGTPackage(gtp);
     dbLanguage.setDownloaded(false);
     }
     }

     mAdapter.updateGTLanguage(dbLanguage);
     }

     return null;
     }

     @Override protected void onPostExecute(Void aVoid) {
     super.onPostExecute(aVoid);
     pdLoading.dismiss();

     SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
     SharedPreferences.Editor editor = settings.edit();
     editor.putString(GTLanguage.KEY_PARALLEL, "");
     editor.commit();

     Toast.makeText(SettingsPW.this, "Translator mode is enabled", Toast.LENGTH_SHORT).show();
     mAdapter.close();
     }
     }
     */
}
