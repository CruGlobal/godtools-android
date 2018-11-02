package org.cru.godtools.base.ui.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import org.cru.godtools.analytics.AnalyticsService;
import org.cru.godtools.base.ui.R2;
import org.greenrobot.eventbus.EventBus;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;
import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {
    // App/Action Bar
    @Nullable
    @BindView(R2.id.appbar)
    protected Toolbar mToolbar;
    @Nullable
    protected ActionBar mActionBar;

    @NonNull
    protected /*final*/ AnalyticsService mAnalytics;
    @NonNull
    protected /*final*/ EventBus mEventBus;

    // region Lifecycle Events

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAnalytics = AnalyticsService.getInstance(this);
        mEventBus = EventBus.getDefault();
    }

    @Override
    @CallSuper
    public void onContentChanged() {
        super.onContentChanged();

        // HACK: manually trigger this ButterKnife view binding to work around an inheritance across libraries bug
        // HACK: see: https://github.com/JakeWharton/butterknife/issues/808
        new BaseActivity_ViewBinding(this);

        ButterKnife.bind(this);
        setupActionBar();
    }

    @CallSuper
    protected void onSetupActionBar() {}

    // endregion Lifecycle Events

    private void setupActionBar() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        // trigger lifecycle event for subclasses
        onSetupActionBar();
    }

    // HACK: workaround this bug: https://issuetracker.google.com/issues/64039135
    @Override
    @SuppressLint("RestrictedApi")
    public Lifecycle getLifecycle() {
        return super.getLifecycle();
    }
}
