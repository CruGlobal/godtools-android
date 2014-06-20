package org.keynote.godtools.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.fragments.LanguageDialogFragment;
import org.keynote.godtools.android.fragments.LanguageDialogFragment.OnLanguageChangedListener;
import org.keynote.godtools.android.fragments.PackageListFragment;
import org.keynote.godtools.android.fragments.PackageListFragment.OnPackageSelectedListener;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.snuffy.SnuffyActivity;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.util.List;

public class MainPW extends FragmentActivity implements OnLanguageChangedListener, OnPackageSelectedListener, DownloadTask.DownloadTaskHandler {
    private static final String PREFS_NAME = "GodTools";
    private static final String TAG_LIST = "PackageList";

    public static final int REFERENCE_DEVICE_HEIGHT = 960;    // pixels on iPhone w/retina - including title bar
    public static final int REFERENCE_DEVICE_WIDTH = 640;    // pixels on iPhone w/retina - full width
    private int mPageLeft;
    private int mPageTop;
    private int mPageWidth;
    private int mPageHeight;

    private List<GTPackage> packageList;
    private String newLangCode;

    PackageListFragment packageFrag;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_pw);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // get the packages for the default language
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String code = settings.getString("languagePrimary", "en");
        packageList = GTPackage.getPackageByLanguage(this, code);

        FragmentManager fm = getSupportFragmentManager();
        packageFrag = (PackageListFragment) fm.findFragmentByTag(TAG_LIST);

        if (packageFrag == null) {
            packageFrag = PackageListFragment.newInstance(packageList);
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.contList, packageFrag, TAG_LIST);
            ft.commit();
        }

        computeDimension();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldUpdateLanguageSettings()) {
            showLanguageDialog();
        }
    }

    private void showLanguageDialog() {
        String phoneLang = Device.getDefaultLanguage();
        DialogFragment frag = LanguageDialogFragment.newInstance(phoneLang, phoneLang);
        frag.show(getSupportFragmentManager(), "");
    }

    private boolean shouldUpdateLanguageSettings() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String langPrimary = settings.getString("languagePrimary", "en");
        String langPhone = Device.getDefaultLanguage();

        return !langPrimary.equalsIgnoreCase(langPhone);
    }

    private void computeDimension() {
        Rect r = new Rect();
        Window w = getWindow();
        w.getDecorView().getWindowVisibleDisplayFrame(r);

        r.top = 0;
        int width = r.width();
        int height = r.height();
        int left = r.left;
        int top = r.top;

        double aspectRatioTarget = (double) Main.REFERENCE_DEVICE_WIDTH / (double) Main.REFERENCE_DEVICE_HEIGHT;
        double aspectRatio = (double) r.width() / (double) r.height();
        if (aspectRatio > aspectRatioTarget) {
            height = r.height();
            width = (int) Math.round(height * aspectRatioTarget);
        } else {
            width = r.width();
            height = (int) Math.round(width / aspectRatioTarget);
        }

        left = r.left + (r.width() - width) / 2;
        top = (r.height() - height) / 2;

        mPageLeft = left;
        mPageTop = top;
        mPageWidth = width;
        mPageHeight = height;
    }

    private void addPageFrameToIntent(Intent intent) {
        intent.putExtra("PageLeft", mPageLeft);
        intent.putExtra("PageTop", mPageTop);
        intent.putExtra("PageWidth", mPageWidth);
        intent.putExtra("PageHeight", mPageHeight);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        packageFrag.disable();
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        packageFrag.enable();
    }

    @Override
    public void onLanguageChanged(String name, String code) {

        newLangCode = code;

        packageList = GTPackage.getPackageByLanguage(MainPW.this, code);
        if (packageList.size() == 0) {

            if (Device.isConnected(MainPW.this)) {
                // TODO: check first if language code is supported
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(), newLangCode, "", this);

            } else {
                // TODO: show dialog, cant set language as default
            }

            Toast.makeText(this, "Language not found", Toast.LENGTH_LONG).show();
        } else {
            // TODO: display packages on list
            Toast.makeText(this, "Display package on list", Toast.LENGTH_LONG).show();

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("languagePrimary", code);
            editor.commit();


        }
    }

    @Override
    public void onPackageSelected(GTPackage gtPackage) {
        Toast.makeText(this, gtPackage.getCode(), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, SnuffyPWActivity.class);
        intent.putExtra("PackageName", gtPackage.getCode());
        intent.putExtra("LanguageCode", gtPackage.getLanguage());
        intent.putExtra("ConfigFileName", gtPackage.getConfigFileName());
        addPageFrameToIntent(intent);
        startActivity(intent);

    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String tag) {

        // TODO: set the language code as default
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("languagePrimary", newLangCode);
        editor.commit();

        // TODO: refresh package list

    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String tag) {

        // TODO: show dialog to inform the user that the download failed
    }
}
