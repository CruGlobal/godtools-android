package org.cru.godtools.base.ui.activity;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Lifecycle;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.cru.godtools.analytics.AnalyticsService;
import org.cru.godtools.base.ui.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {
    // App/Action Bar
    @Nullable
    @BindView(R2.id.appbar)
    protected Toolbar mToolbar;

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
        mAnalytics.onActivityResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAnalytics.onActivityPause(this);
    }

    /* END lifecycle */

    // HACK: workaround this bug: https://issuetracker.google.com/issues/64039135
    @Override
    @SuppressLint("RestrictedApi")
    public Lifecycle getLifecycle() {
        return super.getLifecycle();
    }
}
