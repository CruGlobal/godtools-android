package org.cru.godtools.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.cru.godtools.R;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.fragment.AboutFragment;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_ABOUT;
import static org.cru.godtools.base.util.LocaleUtils.getDeviceLocale;

public final class AboutActivity extends BasePlatformActivity {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    public static void start(@NonNull final Activity context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    /* BEGIN lifecycle */

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
        mEventBus.post(new AnalyticsScreenEvent(SCREEN_ABOUT, getDeviceLocale(this)));
    }

    /* END lifecycle */

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
                .replace(R.id.frame, AboutFragment.newInstance(), TAG_MAIN_FRAGMENT)
                .commit();
    }
}
