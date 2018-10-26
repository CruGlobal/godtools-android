package org.cru.godtools.base.tool.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.cru.godtools.base.tool.R2;
import org.cru.godtools.base.tool.model.view.ManifestViewUtils;
import org.cru.godtools.base.ui.util.DrawableUtils;
import org.cru.godtools.download.manager.DownloadProgress;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.xml.model.Manifest;

import java.util.Locale;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;

import static org.cru.godtools.base.ui.util.LocaleTypefaceUtils.safeApplyTypefaceSpan;
import static org.cru.godtools.download.manager.util.ViewUtils.bindDownloadProgress;

public abstract class BaseToolActivity extends ImmersiveActivity
        implements GodToolsDownloadManager.OnDownloadProgressUpdateListener {
    @Nullable
    protected GodToolsDownloadManager mDownloadManager;

    // App/Action Bar
    @Nullable
    private Menu mToolbarMenu;

    // download progress
    @Nullable
    @BindView(R2.id.loading_progress)
    ProgressBar mLoadingProgress;

    @NonNull
    private DownloadProgress mDownloadProgress = DownloadProgress.INDETERMINATE;

    public BaseToolActivity(final boolean immersive) {
        super(immersive);
    }

    // region Lifecycle Events

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDownloadManager = GodToolsDownloadManager.getInstance(this);
    }

    @Override
    @CallSuper
    public void onContentChanged() {
        // HACK: manually trigger this ButterKnife view binding to work around an inheritance across libraries bug
        // HACK: see: https://github.com/JakeWharton/butterknife/issues/808
        new BaseToolActivity_ViewBinding(this);

        super.onContentChanged();
    }

    @Override
    protected void onSetupActionBar() {
        super.onSetupActionBar();
        updateToolbar();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        mToolbarMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        updateToolbarMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @CallSuper
    protected void onUpdateToolbar() {}

    @Override
    public final void onDownloadProgressUpdated(@Nullable final DownloadProgress progress) {
        mDownloadProgress = progress != null ? progress : DownloadProgress.INDETERMINATE;
        bindDownloadProgress(mLoadingProgress, mDownloadProgress);
    }

    @CallSuper
    protected void onUpdateActiveManifest() {
        updateToolbar();
    }

    // endregion Lifecycle Events

    @Nullable
    protected abstract Manifest getActiveManifest();

    // region Toolbar update logic

    private void updateToolbar() {
        if (mToolbar != null) {
            final Manifest manifest = getActiveManifest();

            // set toolbar background color
            mToolbar.setBackgroundColor(Manifest.getNavBarColor(manifest));

            // set text & controls color
            final int controlColor = Manifest.getNavBarControlColor(manifest);
            mToolbar.setNavigationIcon(DrawableUtils.tint(mToolbar.getNavigationIcon(), controlColor));
            mToolbar.setTitleTextColor(controlColor);
            mToolbar.setSubtitleTextColor(controlColor);
        }

        updateToolbarTitle();
        updateToolbarMenu();
        onUpdateToolbar();
    }

    protected void updateToolbarTitle() {
        setTitle(Manifest.getTitle(getActiveManifest()));
    }

    private void updateToolbarMenu() {
        if (mToolbarMenu != null) {
            // tint all action icons
            final int controlColor = Manifest.getNavBarControlColor(getActiveManifest());
            for (int i = 0; i < mToolbarMenu.size(); ++i) {
                final MenuItem item = mToolbarMenu.getItem(i);
                item.setIcon(DrawableUtils.tint(item.getIcon(), controlColor));
            }
            if (mToolbar != null) {
                mToolbar.setOverflowIcon(DrawableUtils.tint(mToolbar.getOverflowIcon(), controlColor));
            }
        }
    }

    // endregion Toolbar update logic

    // region DownloadProgress logic

    protected final void startDownloadProgressListener(@Nullable final String tool, @Nullable final Locale language) {
        if (mDownloadManager != null && tool != null && language != null) {
            mDownloadManager.addOnDownloadProgressUpdateListener(tool, language, this);
            onDownloadProgressUpdated(mDownloadManager.getDownloadProgress(tool, language));
        }
    }

    protected final void stopDownloadProgressListener() {
        if (mDownloadManager != null) {
            mDownloadManager.removeOnDownloadProgressUpdateListener(this);
        }
    }

    // endregion DownloadProgress logic

    @Override
    public void setTitle(final CharSequence title) {
        super.setTitle(safeApplyTypefaceSpan(title, ManifestViewUtils.getTypeface(getActiveManifest(), this)));
    }
}
