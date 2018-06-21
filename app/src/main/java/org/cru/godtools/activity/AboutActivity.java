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
import org.cru.godtools.fragment.AboutFragment;

import static org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_ABOUT;
import static org.cru.godtools.base.util.LocaleUtils.getDeviceLocale;

public final class AboutActivity extends BasePlatformActivity {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    public static void start(@NonNull final Context context) {
        context.startActivity(new Intent(context, AboutActivity.class));
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_fragment);
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
