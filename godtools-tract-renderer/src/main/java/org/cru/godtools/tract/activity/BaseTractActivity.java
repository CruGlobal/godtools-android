package org.cru.godtools.tract.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.adapter.ManifestPagerAdapter;
import org.cru.godtools.tract.model.Manifest;
import org.cru.godtools.tract.util.DrawableUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseTractActivity extends AppCompatActivity {
    // App/Action Bar
    @BindView(R2.id.appBar)
    Toolbar mToolbar;
    @Nullable
    private ActionBar mActionBar;

    @BindView(R2.id.background_image)
    SimplePicassoImageView mBackgroundImage;

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

        //TODO: enable immersive mode
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
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

        // set toolbar background color
        mToolbar.setBackgroundColor(Color.GREEN);
        mToolbar.setBackgroundColor(Manifest.getPrimaryColor(manifest));

        // set text & controls color
        final int controlColor = Manifest.getPrimaryTextColor(manifest);
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
            mPager.setAdapter(mPagerAdapter);
            updatePager();
        }
    }

    private void updatePager() {
        if (mPagerAdapter != null) {
            mPagerAdapter.setManifest(getActiveManifest());
        }
    }

}
