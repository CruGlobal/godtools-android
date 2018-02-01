package org.cru.godtools.base.ui.activity;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.cru.godtools.analytics.AnalyticsService;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity implements LifecycleRegistryOwner {
    private final LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    @NonNull
    protected /*final*/ AnalyticsService mAnalytics;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAnalytics = AnalyticsService.getInstance(this);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAnalytics.setActiveActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAnalytics.stopAdobeLifecycleTracking();
    }

    /* END lifecycle */

    @Override
    public LifecycleRegistry getLifecycle() {
        return mLifecycleRegistry;
    }
}
