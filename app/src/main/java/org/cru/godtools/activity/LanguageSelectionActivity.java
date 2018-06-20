package org.cru.godtools.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.cru.godtools.R;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.keynote.godtools.android.fragment.LanguagesFragment;

import java.util.Locale;

import static org.cru.godtools.analytics.AnalyticsService.SCREEN_LANGUAGE_SELECTION;

public class LanguageSelectionActivity extends BasePlatformActivity implements LanguagesFragment.Callbacks {
    private static final String EXTRA_PRIMARY = LanguageSelectionActivity.class.getName() + ".PRIMARY";

    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    // these properties should be treated as final and only set/modified in onCreate()
    private /*final*/ boolean mPrimary = true;

    public static void start(@NonNull final Context context, final boolean primary) {
        final Intent intent = new Intent(context, LanguageSelectionActivity.class);
        intent.putExtra(EXTRA_PRIMARY, primary);
        context.startActivity(intent);
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        if (intent != null) {
            mPrimary = intent.getBooleanExtra(EXTRA_PRIMARY, mPrimary);
        }

        setContentView(R.layout.activity_generic_fragment);
    }

    @Override
    protected void onSetupActionBar() {
        super.onSetupActionBar();
        setTitle(mPrimary ? R.string.title_language_primary : R.string.title_language_parallel);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEventBus.post(new AnalyticsScreenEvent(SCREEN_LANGUAGE_SELECTION));
    }

    @Override
    public void onLocaleSelected(@Nullable final Locale locale) {
        GodToolsDownloadManager.getInstance(this).addLanguage(locale);
        storeLocale(locale);
        finish();
    }

    /* END lifecycle */

    private void storeLocale(@Nullable final Locale locale) {
        if (mPrimary) {
            prefs().setPrimaryLanguage(locale);
        } else {
            prefs().setParallelLanguage(locale);
        }
    }

    @MainThread
    private void loadInitialFragmentIfNeeded() {
        final FragmentManager fm = getSupportFragmentManager();

        // short-circuit if there is a currently attached fragment
        Fragment fragment = fm.findFragmentByTag(TAG_MAIN_FRAGMENT);
        if (fragment != null) {
            return;
        }

        // update the displayed fragment
        fm.beginTransaction()
                .replace(R.id.frame, LanguagesFragment.newInstance(mPrimary), TAG_MAIN_FRAGMENT)
                .commit();
    }
}
