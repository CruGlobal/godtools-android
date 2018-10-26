package org.cru.godtools.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.cru.godtools.R;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.fragment.LanguageSettingsFragment;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_LANGUAGE_SETTINGS;
import static org.cru.godtools.base.Settings.FEATURE_LANGUAGE_SETTINGS;

public class LanguageSettingsActivity extends BasePlatformActivity {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    public static void start(@NonNull final Activity activity) {
        activity.startActivity(new Intent(activity, LanguageSettingsActivity.class));
    }

    // region Lifecycle Events

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_fragment_with_nav_drawer);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs().setFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS);
        mEventBus.post(new AnalyticsScreenEvent(SCREEN_LANGUAGE_SETTINGS));
    }

    // endregion Lifecycle Events

    @MainThread
    private void loadInitialFragmentIfNeeded() {
        // short-circuit if there is a currently attached fragment
        final FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(TAG_MAIN_FRAGMENT) != null) {
            return;
        }

        // update the displayed fragment
        fm.beginTransaction()
                .replace(R.id.frame, LanguageSettingsFragment.newInstance(), TAG_MAIN_FRAGMENT)
                .commit();
    }

    @Override
    public void supportNavigateUpTo(@NonNull final Intent upIntent) {
        finish();
    }
}
