package org.keynote.godtools.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.fragments.LanguageDialogFragment;
import org.keynote.godtools.android.fragments.LanguageDialogFragment.OnLanguageChangedListener;
import org.keynote.godtools.android.fragments.PackageListFragment;
import org.keynote.godtools.android.fragments.PackageListFragment.OnPackageSelectedListener;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.util.List;
import java.util.Locale;

public class MainPW extends ActionBarActivity implements OnLanguageChangedListener, OnPackageSelectedListener, DownloadTask.DownloadTaskHandler {
    private static final String PREFS_NAME = "GodTools";
    private static final String TAG_LIST = "PackageList";
    private static final String TAG_DIALOG_LANGUAGE = "LanguageDialog";

    private static final int REQUEST_SETTINGS = 1001;
    public static final int RESULT_DOWNLOAD_PRIMARY = 2001;
    public static final int RESULT_DOWNLOAD_PARALLEL = 2002;
    public static final int RESULT_CHANGED_PRIMARY = 2003;

    public static final int REFERENCE_DEVICE_HEIGHT = 960;    // pixels on iPhone w/retina - including title bar
    public static final int REFERENCE_DEVICE_WIDTH = 640;    // pixels on iPhone w/retina - full width
    private int mPageLeft;
    private int mPageTop;
    private int mPageWidth;
    private int mPageHeight;

    private String languagePrimary;
    private String languagePhone;

    private List<GTPackage> packageList;
    private GTLanguage gtLanguage;

    PackageListFragment packageFrag;
    ProgressBar progressBar;

    boolean isDownloading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_pw);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        languagePhone = Device.getDefaultLanguage();

        // get the packages for the primary language
        packageList = GTPackage.getPackageByLanguage(this, languagePrimary);

        FragmentManager fm = getSupportFragmentManager();
        packageFrag = (PackageListFragment) fm.findFragmentByTag(TAG_LIST);
        if (packageFrag == null) {
            packageFrag = PackageListFragment.newInstance(packageList);
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.contList, packageFrag, TAG_LIST);
            ft.commit();
        }

        computeDimension();

        if (shouldUpdateLanguageSettings()) {
            showLanguageDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_CHANGED_PRIMARY: {
                packageList = GTPackage.getPackageByLanguage(MainPW.this, data.getStringExtra("primaryCode"));
                packageFrag.refreshList(packageList);

                break;
            }
            case RESULT_DOWNLOAD_PRIMARY: {
                // start the download
                String code = data.getStringExtra("primaryCode");
                gtLanguage = GTLanguage.getLanguage(MainPW.this, code);

                showLoading();
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(), gtLanguage.getLanguageCode(), "primary", this);

                break;
            }
            case RESULT_DOWNLOAD_PARALLEL: {

                // refresh the list if the primary language was changed
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String primaryCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");
                if (!languagePrimary.equalsIgnoreCase(primaryCode)) {
                    packageList = GTPackage.getPackageByLanguage(MainPW.this, primaryCode);
                    packageFrag.refreshList(packageList);
                }

                String code = data.getStringExtra("parallelCode");
                gtLanguage = GTLanguage.getLanguage(MainPW.this, code);
                showLoading();
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(), code, "parallel", this);
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
        //createMenuItems(R.menu.main_menu, menu);
        // Inflate the menu items for use in the action bar
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

    private void showLanguageDialog() {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment frag = (DialogFragment) fm.findFragmentByTag(TAG_DIALOG_LANGUAGE);
        if (frag == null) {
            Locale locale = new Locale(languagePhone);
            frag = LanguageDialogFragment.newInstance(locale.getDisplayName(), locale.getLanguage());
            frag.show(fm, TAG_DIALOG_LANGUAGE);
        }
    }

    private boolean shouldUpdateLanguageSettings() {

        // check first if the we support the phones language
        gtLanguage = GTLanguage.getLanguage(this, languagePhone);
        if (gtLanguage == null)
            return false;

        return !languagePrimary.equalsIgnoreCase(languagePhone);
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

    private void showLoading() {
        isDownloading = true;
        supportInvalidateOptionsMenu();
        progressBar.setVisibility(View.VISIBLE);
        packageFrag.disable();
    }

    private void hideLoading() {
        isDownloading = false;
        supportInvalidateOptionsMenu();
        progressBar.setVisibility(View.GONE);
        packageFrag.enable();
    }

    @Override
    public void onLanguageChanged(String name, String code) {

        gtLanguage = GTLanguage.getLanguage(MainPW.this, code);
        if (gtLanguage.isDownloaded()) {
            packageList = GTPackage.getPackageByLanguage(MainPW.this, gtLanguage.getLanguageCode());
            packageFrag.refreshList(packageList);

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, code);

            String parallelLanguage = settings.getString(GTLanguage.KEY_PARALLEL, "");
            if (code.equalsIgnoreCase(parallelLanguage))
                editor.putString(GTLanguage.KEY_PARALLEL, "");

            editor.commit();

        } else {

            if (Device.isConnected(MainPW.this)) {
                showLoading();
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(), gtLanguage.getLanguageCode(), "primary", this);
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
        addPageFrameToIntent(intent);
        startActivity(intent);

    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String tag) {

        if (tag.equalsIgnoreCase("primary")) {
            // set the language code as default
            languagePrimary = gtLanguage.getLanguageCode();

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, gtLanguage.getLanguageCode());
            editor.commit();

            packageList = GTPackage.getPackageByLanguage(MainPW.this, gtLanguage.getLanguageCode());
            packageFrag.refreshList(packageList);

        } else if (tag.equalsIgnoreCase("parallel")) {

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PARALLEL, gtLanguage.getLanguageCode());
            editor.commit();


        }

        // update the database
        gtLanguage.setDownloaded(true);
        gtLanguage.update(MainPW.this);

        hideLoading();
    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String tag) {
        // TODO: show dialog to inform the user that the download failed
        Toast.makeText(MainPW.this, "Failed to download resources", Toast.LENGTH_SHORT).show();
        hideLoading();
    }
}
