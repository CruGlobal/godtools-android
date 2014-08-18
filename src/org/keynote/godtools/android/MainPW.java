package org.keynote.godtools.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.fragments.AlertDialogFragment;
import org.keynote.godtools.android.fragments.LanguageDialogFragment;
import org.keynote.godtools.android.fragments.LanguageDialogFragment.OnLanguageChangedListener;
import org.keynote.godtools.android.fragments.PackageListFragment;
import org.keynote.godtools.android.fragments.PackageListFragment.OnPackageSelectedListener;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class MainPW extends ActionBarActivity implements OnLanguageChangedListener, OnPackageSelectedListener, DownloadTask.DownloadTaskHandler, MetaTask.MetaTaskHandler {
    private static final String PREFS_NAME = "GodTools";
    private static final String TAG_LIST = "PackageList";
    private static final String TAG_DIALOG_LANGUAGE = "LanguageDialog";

    private static final int REQUEST_SETTINGS = 1001;
    public static final int RESULT_DOWNLOAD_PRIMARY = 2001;
    public static final int RESULT_DOWNLOAD_PARALLEL = 2002;
    public static final int RESULT_CHANGED_PRIMARY = 2003;
    public static final int RESULT_PREVIEW_MODE_ENABLED = 1234;
    public static final int RESULT_PREVIEW_MODE_DISABLED = 2345;

    public static final int REFERENCE_DEVICE_HEIGHT = 960;    // pixels on iPhone w/retina - including title bar
    public static final int REFERENCE_DEVICE_WIDTH = 640;    // pixels on iPhone w/retina - full width
    private int mPageLeft;
    private int mPageTop;
    private int mPageWidth;
    private int mPageHeight;

    private String languagePrimary;

    private List<GTPackage> packageList;

    PackageListFragment packageFrag;
    View vLoading;
    ImageButton ibRefresh;
    TextView tvTask;

    boolean isDownloading;
    String authorization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_pw);
        setUpActionBar();

        vLoading = findViewById(R.id.contLoading);
        tvTask = (TextView) findViewById(R.id.tvTask);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        authorization = getString(R.string.key_authorization_generic);

        packageList = getPackageList(); // get the packages for the primary language

        FragmentManager fm = getSupportFragmentManager();
        packageFrag = (PackageListFragment) fm.findFragmentByTag(TAG_LIST);
        if (packageFrag == null) {
            packageFrag = PackageListFragment.newInstance(languagePrimary, packageList);
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.contList, packageFrag, TAG_LIST);
            ft.commit();
        }

        computeDimension();

        String languagePhone = ((SnuffyApplication) getApplication()).getDeviceLocale().getLanguage();
        if (shouldUpdateLanguageSettings(languagePhone)) {
            showLanguageDialog(languagePhone);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_CHANGED_PRIMARY: {

                SnuffyApplication app = (SnuffyApplication) getApplication();
                app.setAppLocale(data.getStringExtra("primaryCode"));

                languagePrimary = data.getStringExtra("primaryCode");
                packageList = getPackageList();
                packageFrag.refreshList(languagePrimary, packageList);

                break;
            }
            case RESULT_DOWNLOAD_PRIMARY: {

                // start the download
                String code = data.getStringExtra("primaryCode");
                showLoading("Downloading resources...");
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        code,
                        "primary",
                        authorization,
                        this);

                break;
            }
            case RESULT_DOWNLOAD_PARALLEL: {

                // refresh the list if the primary language was changed
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String primaryCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");
                if (!languagePrimary.equalsIgnoreCase(primaryCode)) {
                    languagePrimary = primaryCode;
                    packageList = getPackageList();
                    packageFrag.refreshList(languagePrimary, packageList);
                }

                String code = data.getStringExtra("parallelCode");
                showLoading("Downloading resources...");
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        code,
                        "parallel",
                        authorization,
                        this);
                break;
            }
            case RESULT_PREVIEW_MODE_ENABLED: {
                ActionBar actionBar = getSupportActionBar();
                actionBar.setDisplayShowCustomEnabled(true);
                ibRefresh = (ImageButton) findViewById(R.id.ibRefresh);

                // refresh the list
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String primaryCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");

                if (!languagePrimary.equalsIgnoreCase(primaryCode)) {
                    SnuffyApplication app = (SnuffyApplication) getApplication();
                    app.setAppLocale(primaryCode);
                }

                languagePrimary = primaryCode;
                //packageList = getPackageList();
                //packageFrag.refreshList(languagePrimary, packageList);
                showLoading("Downloading drafts...");
                String authorization = settings.getString("authorization", getString(R.string.key_authorization_generic));
                GodToolsApiClient.getListOfDrafts(authorization, primaryCode, "draft_primary", this);

                Toast.makeText(MainPW.this, "Translator preview mode is enabled", Toast.LENGTH_LONG).show();
                break;
            }
            case RESULT_PREVIEW_MODE_DISABLED: {
                ActionBar actionBar = getSupportActionBar();
                actionBar.setDisplayShowCustomEnabled(false);
                ibRefresh = null;

                // refresh the list
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String primaryCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");

                if (!languagePrimary.equalsIgnoreCase(primaryCode)) {
                    SnuffyApplication app = (SnuffyApplication) getApplication();
                    app.setAppLocale(primaryCode);
                }

                languagePrimary = primaryCode;
                packageList = getPackageList();
                packageFrag.refreshList(languagePrimary, packageList);

                Toast.makeText(MainPW.this, "Translator preview mode is disabled", Toast.LENGTH_LONG).show();
                break;
            }
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_settings);
        item.setEnabled(!isDownloading);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsPW.class);
                startActivityForResult(intent, REQUEST_SETTINGS);
                break;
        }

        return true;
    }

    private boolean isTranslatorModeEnabled() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getBoolean("TranslatorMode", false);
    }

    public void refresh(View view) {

        if (Device.isConnected(MainPW.this)) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String authorization = settings.getString("authorization", getString(R.string.key_authorization_generic));
            showLoading("Updating drafts...");
            GodToolsApiClient.getListOfDrafts(authorization, languagePrimary, "draft", this);
        } else {
            Toast.makeText(MainPW.this, "Internet connection is required", Toast.LENGTH_SHORT).show();
        }
    }


    private void setUpActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(R.layout.custom_actionbar);

        if (isTranslatorModeEnabled()) {
            actionBar.setDisplayShowCustomEnabled(true);
        }

    }

    private List<GTPackage> getPackageList() {
        if (isTranslatorModeEnabled()) {
            return GTPackage.getPackageByLanguage(MainPW.this, languagePrimary);
        } else {
            return GTPackage.getLivePackages(MainPW.this, languagePrimary);
        }
    }

    private void showLanguageDialog(String langCode) {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment frag = (DialogFragment) fm.findFragmentByTag(TAG_DIALOG_LANGUAGE);
        if (frag == null) {
            Locale locale = new Locale(langCode);
            frag = LanguageDialogFragment.newInstance(locale.getDisplayName(), locale.getLanguage());
            frag.show(fm, TAG_DIALOG_LANGUAGE);
        }
    }

    private boolean shouldUpdateLanguageSettings(String langCode) {

        // check first if the we support the phones language
        GTLanguage gtLanguage = GTLanguage.getLanguage(this, langCode);
        if (gtLanguage == null)
            return false;

        return !languagePrimary.equalsIgnoreCase(langCode);
    }

    private void computeDimension() {
        Rect r = new Rect();
        Window w = getWindow();
        w.getDecorView().getWindowVisibleDisplayFrame(r);

        mPageLeft = 0;
        mPageTop = 0;
        mPageWidth = r.width();
        mPageHeight = r.height();
    }

    private void addPageFrameToIntent(Intent intent) {
        intent.putExtra("PageLeft", mPageLeft);
        intent.putExtra("PageTop", mPageTop);
        intent.putExtra("PageWidth", mPageWidth);
        intent.putExtra("PageHeight", mPageHeight);
    }

    private void showLoading(String msg) {
        isDownloading = true;
        supportInvalidateOptionsMenu();
        tvTask.setText(msg);
        vLoading.setVisibility(View.VISIBLE);
        packageFrag.disable();

        if (ibRefresh != null) {
            ibRefresh.setEnabled(false);
        }
    }

    private void hideLoading() {
        isDownloading = false;
        supportInvalidateOptionsMenu();
        tvTask.setText("");
        vLoading.setVisibility(View.GONE);
        packageFrag.enable();

        if (ibRefresh != null) {
            ibRefresh.setEnabled(true);
        }
    }

    @Override
    public void onLanguageChanged(String name, String code) {

        GTLanguage gtLanguage = GTLanguage.getLanguage(MainPW.this, code);
        if (gtLanguage.isDownloaded()) {
            languagePrimary = gtLanguage.getLanguageCode();
            packageList = getPackageList();
            packageFrag.refreshList(languagePrimary, packageList);

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, code);

            String parallelLanguage = settings.getString(GTLanguage.KEY_PARALLEL, "");
            if (code.equalsIgnoreCase(parallelLanguage))
                editor.putString(GTLanguage.KEY_PARALLEL, "");

            editor.commit();

            SnuffyApplication app = (SnuffyApplication) getApplication();
            app.setAppLocale(code);


        } else {

            if (Device.isConnected(MainPW.this)) {
                showLoading("Downloading resources...");
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        code,
                        "primary",
                        authorization,
                        this);
            } else {
                // TODO: show dialog, Internet connection is required to download the resources
                Toast.makeText(this, "Unable to download resources. Internet connection unavailable.", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onPackageSelected(GTPackage gtPackage) {

        Intent intent = new Intent(this, SnuffyPWActivity.class);
        intent.putExtra("PackageName", gtPackage.getCode());
        intent.putExtra("LanguageCode", gtPackage.getLanguage());
        intent.putExtra("ConfigFileName", gtPackage.getConfigFileName());
        intent.putExtra("Status", gtPackage.getStatus());
        addPageFrameToIntent(intent);
        startActivity(intent);

    }

    @Override
    public void metaTaskComplete(InputStream is, String langCode, String tag) {
        // process the input stream
        new UpdateDraftListTask().execute(is, langCode, tag);
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag) {

        if (tag.equalsIgnoreCase("primary")) {

            languagePrimary = langCode;

            SnuffyApplication app = (SnuffyApplication) getApplication();
            app.setAppLocale(langCode);

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, langCode);
            editor.commit();

            GTLanguage gtl = GTLanguage.getLanguage(MainPW.this, langCode);
            gtl.setDownloaded(true);
            gtl.update(MainPW.this);

            if (isTranslatorModeEnabled()) {
                // check for draft_primary
                String authorization = settings.getString("authorization", getString(R.string.key_authorization_generic));
                GodToolsApiClient.getListOfDrafts(authorization, langCode, "draft_primary", this);

            } else {
                packageList = getPackageList();
                packageFrag.refreshList(langCode, packageList);
                hideLoading();
            }

        } else if (tag.equalsIgnoreCase("parallel")) {

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PARALLEL, langCode);
            editor.commit();

            GTLanguage gtl = GTLanguage.getLanguage(MainPW.this, langCode);
            gtl.setDownloaded(true);
            gtl.update(MainPW.this);

            if (isTranslatorModeEnabled()) {
                // check for draft_parallel
                String authorization = settings.getString("authorization", getString(R.string.key_authorization_generic));
                GodToolsApiClient.getListOfDrafts(authorization, langCode, "draft_parallel", this);

            } else {
                hideLoading();
            }

        } else if (tag.equalsIgnoreCase("draft")) {

            Toast.makeText(MainPW.this, "Drafts have been updated", Toast.LENGTH_SHORT).show();
            packageList = getPackageList();
            packageFrag.refreshList(langCode, packageList);
            hideLoading();

        } else if (tag.equalsIgnoreCase("draft_primary")) {

            languagePrimary = langCode;
            packageList = getPackageList();
            packageFrag.refreshList(langCode, packageList);
            hideLoading();

        } else if (tag.equalsIgnoreCase("draft_parallel")) {

            hideLoading();

        }

    }

    @Override
    public void metaTaskFailure(InputStream is, String langCode, String tag) {

        if (tag.equalsIgnoreCase("draft") || tag.equalsIgnoreCase("draft_primary")) {
            packageList = getPackageList();
            packageFrag.refreshList(langCode, packageList);
        }

        hideLoading();
        Toast.makeText(MainPW.this, "Failed to update drafts", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag) {

        if (tag.equalsIgnoreCase("draft")) {

            Toast.makeText(MainPW.this, "Failed to update drafts", Toast.LENGTH_SHORT).show();

        } else if (tag.equalsIgnoreCase("draft_primary")) {

            packageList = getPackageList();
            packageFrag.refreshList(langCode, packageList);
            Toast.makeText(MainPW.this, "Failed to download drafts", Toast.LENGTH_SHORT).show();

        } else if (tag.equalsIgnoreCase("draft_parallel")) {

            // do nothing

        }
        else if (tag.equalsIgnoreCase("primary") || tag.equalsIgnoreCase("parallel")){

            Toast.makeText(MainPW.this, "Failed to download resources", Toast.LENGTH_SHORT).show();

        }

        hideLoading();
    }

    private class UpdateDraftListTask extends AsyncTask<Object, Void, Boolean> {
        boolean mNewDraftsAvailable;
        String tag, langCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mNewDraftsAvailable = false;
        }

        @Override
        protected Boolean doInBackground(Object... params) {

            InputStream is = (InputStream) params[0];
            langCode = params[1].toString();
            tag = params[2].toString();

            List<GTLanguage> languageList = GTPackageReader.processMetaResponse(is);

            GTLanguage language = languageList.get(0);
            List<GTPackage> packagesDraft = language.getPackages();

            return packagesDraft.size() != 0;
        }

        @Override
        protected void onPostExecute(Boolean shouldDownload) {
            super.onPostExecute(shouldDownload);

            if (shouldDownload) {

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String authorization = settings.getString("authorization", getString(R.string.key_authorization_generic));
                GodToolsApiClient.downloadDrafts((SnuffyApplication) getApplication(), authorization, langCode, tag, MainPW.this);


            } else {

                if (tag.equalsIgnoreCase("draft")) {

                    FragmentManager fm = getSupportFragmentManager();
                    DialogFragment frag = (DialogFragment) fm.findFragmentByTag("alert_dialog");
                    if (frag == null) {
                        Locale primary = new Locale(langCode);
                        frag = AlertDialogFragment.newInstance("Drafts", String.format("No drafts available for %s", primary.getDisplayName()));
                        frag.setCancelable(false);
                        frag.show(fm, "alert_dialog");
                    }

                } else if (tag.equalsIgnoreCase("draft_primary")){

                    languagePrimary = langCode;
                    packageList = getPackageList();
                    packageFrag.refreshList(langCode, packageList);

                } else if (tag.equalsIgnoreCase("draft_parallel")) {
                    // do nothing
                }

                hideLoading();
            }
        }
    }
}
