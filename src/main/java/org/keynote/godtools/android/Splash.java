package org.keynote.godtools.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.cru.godtools.api.GodToolsApi;
import org.cru.godtools.api.model.GTLanguages;
import org.cru.godtools.sync.GodToolsSyncService;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.dao.DBContract.GTLanguageTable;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.PackageDownloadHelper;
import org.keynote.godtools.android.service.UpdatePackageListTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.keynote.godtools.android.Constants.PREF_PRIMARY_LANGUAGE;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.FIRST_LAUNCH;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;

/*
    Logic flow:

    If first load:
        - unpack bundled content
        - query API for latest available languages and packages
        - store latest languages and packages in local database
        - download initial content for device language, if available

    If not first load:
        - go to main activity (home screen)
 */
public class Splash extends Activity implements DownloadTask.DownloadTaskHandler {
    private static final String TAG = Splash.class.getSimpleName();
    @BindView(R.id.tvTask)
    TextView mUpdateText;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;

    private SharedPreferences settings;

    /* BEGIN lifecycle */

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        syncData();

        setContentView(R.layout.splash_pw);
        ButterKnife.bind(this);
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // first use
        if (isFirstLaunch()) {
            Log.i(TAG, "First Launch");

            // set primary language on first start
            settings.edit().putString(PREF_PRIMARY_LANGUAGE, Locale.getDefault().getLanguage()).apply();

            showLoading();
            GodToolsApi.getInstance(this).legacy.getListOfPackages().enqueue(new Callback<GTLanguages>() {
                @Override
                public void onResponse(Call<GTLanguages> call, Response<GTLanguages> response) {
                    UpdatePackageListTask.run(response.body().mLanguages, GodToolsDao.getInstance(Splash.this));

                    // if the API has packages available for the device default language then download them
                    // this is determined by going through the results of the meta download
                    if (apiHasDeviceDefaultLanguage(response.body().mLanguages)) {
                        PackageDownloadHelper.downloadLanguagePack(
                                getApp(),
                                Locale.getDefault().getLanguage(),
                                "primary",
                                Splash.this);
                    }
                    // if not, then switch back to English and download those latest resources
                    else {
                        settings.edit().putString(PREF_PRIMARY_LANGUAGE, ENGLISH_DEFAULT).apply();

                        PackageDownloadHelper.downloadLanguagePack(
                                getApp(),
                                ENGLISH_DEFAULT,
                                "primary",
                                Splash.this);
                    }
                }

                @Override
                public void onFailure(Call<GTLanguages> call, Throwable t) {
                    goToMainActivity();
                }
            });
        }
        // all updates are complete, proceed directly to Main Activity
        else {
            goToMainActivity();
        }
    }

    /* END lifecycle */

    private void syncData() {
        GodToolsSyncService.syncLanguages(this, false).sync();
    }

    private boolean isFirstLaunch() {
        return settings.getBoolean(FIRST_LAUNCH, true);
    }

    private void showLoading() {
        mUpdateText.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    void goToMainActivity() {
        // so now that we are expiring the translator code after 12 hours we will auto "log out" the
        // user when the app is restarted.

        if (settings.getBoolean(TRANSLATOR_MODE, false)) {
            settings.edit().putBoolean(TRANSLATOR_MODE, false).apply();
        }

        Intent intent = new Intent(this, MainPW.class);
        startActivity(intent);
        finish();
    }

    private SnuffyApplication getApp() {
        return (SnuffyApplication) getApplication();
    }

    private boolean apiHasDeviceDefaultLanguage(List<GTLanguage> languageList) {
        for (GTLanguage metaLanguageFromInitialDownload : languageList) {
            if (metaLanguageFromInitialDownload.getLanguageCode().equalsIgnoreCase(Locale.getDefault().getLanguage()) &&
                    !metaLanguageFromInitialDownload.getPackages().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag) {
        final GTLanguage language = new GTLanguage();
        language.setLanguageCode(langCode);
        language.setDownloaded(true);
        GodToolsDao.getInstance(this).updateAsync(language, GTLanguageTable.COL_DOWNLOADED);

        goToMainActivity();
    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag) {
        // if there was an error downloading resources, then switch the phone's language back to English since those are the
        // resources that were bundled.  user would get a blank screen if not
        settings.edit().putString(PREF_PRIMARY_LANGUAGE, ENGLISH_DEFAULT).apply();
        goToMainActivity();
    }
}
