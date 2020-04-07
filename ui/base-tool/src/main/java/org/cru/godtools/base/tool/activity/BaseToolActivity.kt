package org.cru.godtools.base.tool.activity

import android.content.Intent
import android.os.AsyncTask
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.CallSuper
import androidx.core.view.forEach
import butterknife.BindView
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import org.ccci.gto.android.common.util.WeakTask
import org.cru.godtools.base.Settings
import org.cru.godtools.base.tool.R
import org.cru.godtools.base.tool.R2
import org.cru.godtools.base.tool.analytics.model.FirstToolOpened
import org.cru.godtools.base.tool.analytics.model.ShareActionEvent
import org.cru.godtools.base.tool.analytics.model.ToolOpened
import org.cru.godtools.base.tool.model.view.ManifestViewUtils
import org.cru.godtools.base.ui.util.applyTypefaceSpan
import org.cru.godtools.base.ui.util.tint
import org.cru.godtools.download.manager.DownloadProgress
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.download.manager.GodToolsDownloadManager.OnDownloadProgressUpdateListener
import org.cru.godtools.download.manager.databinding.bindProgress
import org.cru.godtools.model.event.ToolUsedEvent
import org.cru.godtools.xml.model.Manifest
import org.keynote.godtools.android.db.GodToolsDao
import java.util.Locale
import javax.inject.Inject

abstract class BaseToolActivity(immersive: Boolean) : ImmersiveActivity(immersive), OnDownloadProgressUpdateListener {
    @Inject
    internal lateinit var dao: GodToolsDao
    protected val downloadManager by lazy { GodToolsDownloadManager.getInstance(this) }

    // region Lifecycle
    @CallSuper
    override fun onContentChanged() {
        // HACK: manually trigger this ButterKnife view binding to work around an inheritance across libraries bug
        // HACK: see: https://github.com/JakeWharton/butterknife/issues/808
        BaseToolActivity_ViewBinding(this)

        super.onContentChanged()
        updateVisibilityState()
    }

    override fun onSetupActionBar() {
        super.onSetupActionBar()
        updateToolbar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_tool, menu)
        menu.setupToolbarMenu()
        menu.setupShareMenuItem()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        updateToolbarMenu()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()
        syncTools()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_share -> {
            shareCurrentTool()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @CallSuper
    protected open fun onUpdateToolbar() = Unit

    @CallSuper
    protected open fun onUpdateActiveManifest() {
        updateToolbar()
        updateVisibilityState()
    }
    // endregion Lifecycle

    /**
     * @return The currently active manifest that is a valid supported type for this activity, otherwise return null.
     */
    protected abstract val activeManifest: Manifest?

    // region Toolbar update logic
    private var toolbarMenu: Menu? = null

    private fun Menu.setupToolbarMenu() {
        toolbarMenu = this
    }

    private fun updateToolbar() {
        toolbar?.apply {
            val manifest = activeManifest

            // set toolbar background color
            setBackgroundColor(Manifest.getNavBarColor(manifest))

            // set text & controls color
            val controlColor = Manifest.getNavBarControlColor(manifest)
            navigationIcon = toolbar!!.navigationIcon.tint(controlColor)
            setTitleTextColor(controlColor)
            setSubtitleTextColor(controlColor)
        }
        updateToolbarTitle()
        updateToolbarMenu()
        onUpdateToolbar()
    }

    protected open fun updateToolbarTitle() {
        title = Manifest.getTitle(activeManifest).orEmpty()
    }

    private fun updateToolbarMenu() {
        // tint all action icons
        val controlColor = Manifest.getNavBarControlColor(activeManifest)
        toolbar?.apply { overflowIcon = overflowIcon.tint(controlColor) }
        toolbarMenu?.forEach { it.icon = it.icon.tint(controlColor) }

        updateShareMenuItem()
    }
    // endregion Toolbar update logic

    // region Share tool logic
    private var shareMenuItem: MenuItem? = null

    private fun Menu.setupShareMenuItem() {
        shareMenuItem = findItem(R.id.action_share)
    }

    protected fun updateShareMenuItem() {
        shareMenuItem?.isVisible = hasShareLinkUri()
    }

    protected open fun hasShareLinkUri() = shareLinkUri != null
    protected open val shareLinkTitle get() = activeManifest?.title
    protected open val shareLinkUri: String? get() = null

    private fun shareCurrentTool() {
        // short-circuit if we don't have a share tool url
        val shareUrl = shareLinkUri ?: return

        // track the share action
        eventBus.post(ShareActionEvent)

        // start the share activity chooser with our share link
        val title = shareLinkTitle
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_tool_subject, title))
            putExtra(Intent.EXTRA_TEXT, shareUrl)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_tool_title, title)))
    }
    // endregion Share tool logic

    // region Tool state
    @JvmField
    @BindView(R2.id.contentLoading)
    internal var loadingContent: View? = null

    @JvmField
    @BindView(R2.id.noContent)
    internal var missingContent: View? = null

    @JvmField
    @BindView(R2.id.mainContent)
    internal var mainContent: View? = null

    @CallSuper
    protected open fun updateVisibilityState() {
        val state = determineActiveToolState()
        loadingContent?.visibility = if (state == STATE_LOADING) View.VISIBLE else View.GONE
        mainContent?.visibility = if (state == STATE_LOADED) View.VISIBLE else View.GONE
        missingContent?.visibility =
            if (state == STATE_NOT_FOUND || state == STATE_INVALID_TYPE) View.VISIBLE else View.GONE
    }

    protected abstract fun determineActiveToolState(): Int
    // endregion Tool state

    // region Tool sync/download logic
    private var syncToolsState: ListenableFuture<*>? = null
    protected val isSyncToolsDone get() = syncToolsState?.isDone == true

    private fun syncTools() {
        cacheTools()
        val task = SyncToolsRunnable(this, WeakTask(this, TASK_CACHE_TOOLS))
            .also { AsyncTask.THREAD_POOL_EXECUTOR.execute(it) }

        // track sync tools state, combining previous state with current state
        syncToolsState = when (syncToolsState?.isDone) {
            false -> Futures.successfulAsList(syncToolsState, task.future)
            else -> task.future
        }
    }

    protected abstract fun cacheTools()
    // endregion Tool sync/download logic

    // region DownloadProgress logic
    @JvmField
    @BindView(R2.id.loading_progress)
    internal var loadingProgress: ProgressBar? = null

    protected fun startDownloadProgressListener(tool: String?, language: Locale?) {
        if (tool != null && language != null) {
            downloadManager.addOnDownloadProgressUpdateListener(tool, language, this)
            onDownloadProgressUpdated(downloadManager.getDownloadProgress(tool, language))
        }
    }

    override fun onDownloadProgressUpdated(progress: DownloadProgress?) {
        // TODO: move this to data binding
        loadingProgress?.bindProgress(progress ?: DownloadProgress.INDETERMINATE)
    }

    protected fun stopDownloadProgressListener() {
        downloadManager.removeOnDownloadProgressUpdateListener(this)
    }
    // endregion DownloadProgress logic

    protected fun trackToolOpen(tool: String) {
        eventBus.post(ToolUsedEvent(tool))

        val settings = Settings.getInstance(this)
        eventBus.post(if (settings.isFeatureDiscovered(Settings.FEATURE_TOOL_OPENED)) ToolOpened else FirstToolOpened)
        settings.setFeatureDiscovered(Settings.FEATURE_TOOL_OPENED)

        dao.updateSharesDeltaAsync(tool, 1)
    }

    override fun setTitle(title: CharSequence) =
        super.setTitle(title.applyTypefaceSpan(ManifestViewUtils.getTypeface(activeManifest, this)))

    companion object {
        const val STATE_LOADING = 0
        const val STATE_LOADED = 1
        const val STATE_NOT_FOUND = 2
        const val STATE_INVALID_TYPE = 3

        private val TASK_CACHE_TOOLS = WeakTask.Task<BaseToolActivity> { it.cacheTools() }
    }
}
