package org.cru.godtools.base.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.cru.godtools.analytics.AnalyticsService;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {
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
}
