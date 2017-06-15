package org.cru.godtools.tract.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.adapter.ManifestPagerAdapter;
import org.cru.godtools.tract.model.Manifest;
import org.cru.godtools.tract.util.DrawableUtils;
import org.cru.godtools.tract.widget.ScaledPicassoImageView;

import butterknife.BindView;

public abstract class BaseTractActivity extends ImmersiveActivity implements ManifestPagerAdapter.Callbacks {
    // App/Action Bar
    @BindView(R2.id.appBar)
    Toolbar mToolbar;
    @Nullable
    private ActionBar mActionBar;

    @BindView(R2.id.background_image)
    ScaledPicassoImageView mBackgroundImage;

    // Manifest page pager
    @Nullable
    @BindView(R2.id.pages)
    ViewPager mPager;
    @Nullable
    ManifestPagerAdapter mPagerAdapter;

    private boolean mPrimaryActive = true;
    @Nullable
    private Manifest mPrimaryManifest;
    @Nullable
    private Manifest mParallelManifest;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tract);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        setupActionBar();
        setupPager();
    }

    protected void onSetupActionBar(@NonNull final ActionBar actionBar) {}

    protected void onUpdateActionBar(@NonNull final ActionBar actionBar) {}

    /* END lifecycle */

    private void setupActionBar() {
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            onSetupActionBar(mActionBar);
        }
        updateActionBar();
    }

    protected final void updateActionBar() {
        final ActionBar actionBar = mActionBar;
        if (actionBar != null) {
            onUpdateActionBar(actionBar);
        }
    }

    protected void setPrimaryManifest(@Nullable final Manifest manifest) {
        mPrimaryManifest = manifest;
        if (mPrimaryActive) {
            updateActiveManifest();
        }
    }

    protected void setParallelManifest(@Nullable final Manifest manifest) {
        mParallelManifest = manifest;
        if (!mPrimaryActive) {
            updateActiveManifest();
        }
    }

    @Nullable
    protected Manifest getActiveManifest() {
        return mPrimaryActive ? mPrimaryManifest : mParallelManifest;
    }

    private void updateActiveManifest() {
        updateToolbar();
        updateBackground();
        updatePager();
    }

    private void updateToolbar() {
        final Manifest manifest = getActiveManifest();
        setTitle(Manifest.getTitle(manifest));

        // set toolbar background color
        mToolbar.setBackgroundColor(Manifest.getNavBarColor(manifest));

        // set text & controls color
        final int controlColor = Manifest.getNavBarControlColor(manifest);
        mToolbar.setTitleTextColor(controlColor);
        mToolbar.setSubtitleTextColor(controlColor);
        mToolbar.setNavigationIcon(DrawableUtils.tint(mToolbar.getNavigationIcon(), controlColor));
    }

    private void updateBackground() {
        final Manifest manifest = getActiveManifest();
        getWindow().getDecorView().setBackgroundColor(Manifest.getBackgroundColor(manifest));
        Manifest.bindBackgroundImage(manifest, mBackgroundImage);
    }

    private void setupPager() {
        if (mPager != null) {
            mPagerAdapter = new ManifestPagerAdapter();
            mPagerAdapter.setCallbacks(this);
            mPager.setAdapter(mPagerAdapter);
            updatePager();
        }
    }

    @Override
    public void goToPage(final int position) {
        if (mPager != null) {
            mPager.setCurrentItem(position);
        }
    }

    private void updatePager() {
        if (mPagerAdapter != null) {
            mPagerAdapter.setManifest(getActiveManifest());
        }
    }
}
