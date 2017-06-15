package org.cru.godtools.tract.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.BundleUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.adapter.ManifestPagerAdapter;
import org.cru.godtools.tract.content.TractManifestLoader;
import org.cru.godtools.tract.model.Manifest;
import org.cru.godtools.tract.util.DrawableUtils;
import org.cru.godtools.tract.widget.ScaledPicassoImageView;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Tool;

import java.util.Locale;

import butterknife.BindView;

import static org.cru.godtools.base.Constants.EXTRA_PARALLEL_LANGUAGE;
import static org.cru.godtools.base.Constants.EXTRA_PRIMARY_LANGUAGE;
import static org.cru.godtools.base.Constants.EXTRA_TOOL;

public class TractActivity extends ImmersiveActivity implements ManifestPagerAdapter.Callbacks {
    static final int LOADER_MANIFEST_PRIMARY = 101;
    static final int LOADER_MANIFEST_PARALLEL = 102;

    // App/Action Bar
    @BindView(R2.id.appBar)
    Toolbar mToolbar;
    @Nullable
    private Menu mToolbarMenu;
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

    /*final*/ long mTool = Tool.INVALID_ID;
    @NonNull
    /*final*/ Locale mPrimaryLocale = Language.INVALID_CODE;
    @Nullable
    /*final*/ Locale mParallelLocale = null;

    private boolean mPrimaryActive = true;
    @Nullable
    private Manifest mPrimaryManifest;
    @Nullable
    private Manifest mParallelManifest;

    protected static void populateExtras(@NonNull final Bundle extras, final long toolId, @NonNull final Locale primary,
                                         @Nullable final Locale parallel) {
        extras.putLong(EXTRA_TOOL, toolId);
        BundleUtils.putLocale(extras, EXTRA_PRIMARY_LANGUAGE, primary);
        BundleUtils.putLocale(extras, EXTRA_PARALLEL_LANGUAGE, parallel);
    }

    public static void start(@NonNull final Context context, final long toolId, @NonNull final Locale primary,
                             @Nullable final Locale parallel) {
        final Bundle extras = new Bundle();
        populateExtras(extras, toolId, primary, parallel);
        context.startActivity(new Intent(context, TractActivity.class).putExtras(extras));
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tract);

        final Intent intent = getIntent();
        final Bundle extras = intent != null ? intent.getExtras() : null;
        if (extras != null) {
            mTool = extras.getLong(EXTRA_TOOL, mTool);
            //noinspection ConstantConditions
            mPrimaryLocale = BundleUtils.getLocale(extras, EXTRA_PRIMARY_LANGUAGE, mPrimaryLocale);
            mParallelLocale = BundleUtils.getLocale(extras, EXTRA_PARALLEL_LANGUAGE, mParallelLocale);
        }

        startLoaders();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        setupToolbar();
        setupPager();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_tract, menu);
        mToolbarMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        updateToolbarMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    /* END lifecycle */

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        updateToolbar();
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

        updateToolbarMenu();
    }

    private void updateToolbarMenu() {
        if (mToolbarMenu != null) {
            // tint all action icons
            final int controlColor = Manifest.getNavBarControlColor(getActiveManifest());
            for (int i = 0; i < mToolbarMenu.size(); ++i) {
                final MenuItem item = mToolbarMenu.getItem(i);
                item.setIcon(DrawableUtils.tint(item.getIcon(), controlColor));
            }
        }
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

    private void startLoaders() {
        final LoaderManager manager = getSupportLoaderManager();

        final ManifestLoaderCallbacks manifestCallbacks = new ManifestLoaderCallbacks();
        manager.initLoader(LOADER_MANIFEST_PRIMARY, null, manifestCallbacks);
        if (mParallelLocale != null) {
            manager.initLoader(LOADER_MANIFEST_PARALLEL, null, manifestCallbacks);
        }
    }

    class ManifestLoaderCallbacks extends SimpleLoaderCallbacks<Manifest> {
        @Nullable
        @Override
        public Loader<Manifest> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MANIFEST_PRIMARY:
                    return new TractManifestLoader(TractActivity.this, mTool, mPrimaryLocale);
                case LOADER_MANIFEST_PARALLEL:
                    if (mParallelLocale != null) {
                        return new TractManifestLoader(TractActivity.this, mTool, mParallelLocale);
                    }
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Manifest> loader, @Nullable final Manifest manifest) {
            switch (loader.getId()) {
                case LOADER_MANIFEST_PRIMARY:
                    setPrimaryManifest(manifest);
                    break;
                case LOADER_MANIFEST_PARALLEL:
                    setParallelManifest(manifest);
                    break;
            }
        }
    }
}
