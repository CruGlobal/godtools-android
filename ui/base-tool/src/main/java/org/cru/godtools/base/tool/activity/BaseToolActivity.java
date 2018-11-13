package org.cru.godtools.base.tool.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.ccci.gto.android.common.util.WeakRunnable;
import org.cru.godtools.base.tool.R;
import org.cru.godtools.base.tool.R2;
import org.cru.godtools.base.tool.model.view.ManifestViewUtils;
import org.cru.godtools.base.ui.util.DrawableUtils;
import org.cru.godtools.download.manager.DownloadProgress;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.sync.task.ToolSyncTasks;
import org.cru.godtools.xml.model.Manifest;

import java.io.IOException;
import java.util.Locale;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;

import static org.cru.godtools.base.ui.util.LocaleTypefaceUtils.safeApplyTypefaceSpan;
import static org.cru.godtools.download.manager.util.ViewUtils.bindDownloadProgress;

public abstract class BaseToolActivity extends ImmersiveActivity
        implements GodToolsDownloadManager.OnDownloadProgressUpdateListener {
    protected static final int STATE_LOADING = 0;
    protected static final int STATE_LOADED = 1;
    protected static final int STATE_NOT_FOUND = 2;

    @Nullable
    protected GodToolsDownloadManager mDownloadManager;

    // App/Action Bar
    @Nullable
    private Menu mToolbarMenu;
    @Nullable
    private MenuItem mShareMenuItem;

    // Visibility sections
    @Nullable
    @BindView(R2.id.contentLoading)
    View mLoadingContent;
    @Nullable
    @BindView(R2.id.noContent)
    View mMissingContent;
    @Nullable
    @BindView(R2.id.mainContent)
    View mMainContent;

    // download progress
    @Nullable
    @BindView(R2.id.loading_progress)
    ProgressBar mLoadingProgress;

    @Nullable
    private ListenableFuture<?> mSyncToolsState = null;
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
        updateVisibilityState();
    }

    @Override
    protected void onSetupActionBar() {
        super.onSetupActionBar();
        updateToolbar();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_tool, menu);
        mToolbarMenu = menu;
        mShareMenuItem = menu.findItem(R.id.action_share);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        updateToolbarMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        syncTools();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_share) {
            shareCurrentTool();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        updateVisibilityState();
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
        updateShareMenuItem();
    }

    // endregion Toolbar update logic

    // region Share tool logic

    protected void updateShareMenuItem() {
        if (mShareMenuItem != null) {
            mShareMenuItem.setVisible(hasShareLinkUri());
        }
    }

    private void shareCurrentTool() {
        // short-circuit if we don't have a share tool url
        final String shareUrl = getShareLinkUri();
        if (shareUrl == null) {
            return;
        }

        // track the share action
        mAnalytics.onTrackShareAction();

        // start the share activity chooser with our share link
        final String title = getShareLinkTitle();
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_tract_subject, title));
        intent.putExtra(Intent.EXTRA_TEXT, shareUrl);
        startActivity(Intent.createChooser(intent, getString(R.string.share_tract_title, title)));
    }

    protected boolean hasShareLinkUri() {
        return getShareLinkUri() != null;
    }

    @Nullable
    protected String getShareLinkTitle() {
        final Manifest manifest = getActiveManifest();
        return manifest != null ? manifest.getTitle() : null;
    }

    @Nullable
    protected String getShareLinkUri() {
        return null;
    }

    // endregion Share tool logic

    // region Tool state

    @CallSuper
    protected void updateVisibilityState() {
        final int state = determineActiveToolState();

        if (mLoadingContent != null) {
            mLoadingContent.setVisibility(state == STATE_LOADING ? View.VISIBLE : View.GONE);
        }
        if (mMainContent != null) {
            mMainContent.setVisibility(state == STATE_LOADED ? View.VISIBLE : View.GONE);
        }
        if (mMissingContent != null) {
            mMissingContent.setVisibility(state == STATE_NOT_FOUND ? View.VISIBLE : View.GONE);
        }
    }

    protected abstract int determineActiveToolState();

    // endregion Tool state

    // region Tool sync/download logic

    protected boolean isSyncToolsDone() {
        return mSyncToolsState != null && mSyncToolsState.isDone();
    }

    private void syncTools() {
        cacheTools();
        final SyncToolsRunnable task = new SyncToolsRunnable(this, this::cacheTools);
        AsyncTask.THREAD_POOL_EXECUTOR.execute(task);

        // track sync tools state, combining previous state with current state
        if (mSyncToolsState == null || mSyncToolsState.isDone()) {
            mSyncToolsState = task.mFuture;
        } else {
            mSyncToolsState = Futures.successfulAsList(mSyncToolsState, task.mFuture);
        }
    }

    protected abstract void cacheTools();

    // endregion Tool sync/download logic

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

    static class SyncToolsRunnable implements Runnable {
        private final Context mContext;
        private final WeakRunnable mPostSyncTask;
        final SettableFuture<?> mFuture = SettableFuture.create();

        SyncToolsRunnable(@NonNull final Context context, @NonNull final Runnable postSyncTask) {
            mContext = context.getApplicationContext();
            mPostSyncTask = new WeakRunnable(postSyncTask);
        }

        @Override
        public void run() {
            try {
                new ToolSyncTasks(mContext).syncTools(Bundle.EMPTY);
            } catch (final IOException ignored) {
            }
            mPostSyncTask.run();
            mFuture.set(null);
        }
    }
}
