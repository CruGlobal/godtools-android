package org.cru.godtools.base.tool.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.view.forEach
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import butterknife.BindView
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.base.Constants
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_TOOL_SHARE
import org.cru.godtools.base.tool.R
import org.cru.godtools.base.tool.R2
import org.cru.godtools.base.tool.analytics.model.FirstToolOpened
import org.cru.godtools.base.tool.analytics.model.ShareActionEvent
import org.cru.godtools.base.tool.analytics.model.ToolOpened
import org.cru.godtools.base.tool.model.view.ManifestViewUtils
import org.cru.godtools.base.ui.util.applyTypefaceSpan
import org.cru.godtools.base.ui.util.getShareMessage
import org.cru.godtools.base.ui.util.tint
import org.cru.godtools.download.manager.DownloadProgress
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.download.manager.GodToolsDownloadManager.OnDownloadProgressUpdateListener
import org.cru.godtools.download.manager.databinding.bindProgress
import org.cru.godtools.model.event.ToolUsedEvent
import org.cru.godtools.sync.task.ToolSyncTasks
import org.cru.godtools.xml.model.Manifest
import org.keynote.godtools.android.db.GodToolsDao
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

abstract class BaseToolActivity(
    immersive: Boolean,
    @LayoutRes contentLayoutId: Int = Constants.INVALID_LAYOUT_RES
) : ImmersiveActivity(immersive, contentLayoutId), OnDownloadProgressUpdateListener {
    @Inject
    internal lateinit var dao: GodToolsDao
    @Inject
    protected lateinit var downloadManager: GodToolsDownloadManager

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
    protected val activeManifest get() = activeManifestLiveData.value
    protected abstract val activeManifestLiveData: LiveData<Manifest?>

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
        showNextFeatureDiscovery()
    }

    protected open fun hasShareLinkUri() = shareLinkUri != null
    protected open val shareLinkTitle get() = activeManifest?.title
    protected open val shareLinkUri: String? get() = null

    private fun shareCurrentTool() {
        // short-circuit if we don't have a share tool url
        val shareUrl = shareLinkUri ?: return

        // track the share action
        eventBus.post(ShareActionEvent)
        settings.setFeatureDiscovered(FEATURE_TOOL_SHARE)

        // start the share activity chooser with our share link
        val title = shareLinkTitle
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_tool_subject, title))
            putExtra(Intent.EXTRA_TEXT, getShareMessage(shareUrl))
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
    @Inject
    internal lateinit var toolSyncTasks: ToolSyncTasks
    protected val isInitialSyncFinished = MutableLiveData<Boolean>()

    private fun syncTools() = lifecycleScope.launch(Dispatchers.Main.immediate) {
        cacheTools()
        try {
            toolSyncTasks.syncTools(Bundle.EMPTY)
            isInitialSyncFinished.value = true
        } catch (ignored: IOException) {
        }
        cacheTools()
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

    // region Feature Discovery
    private var featureDiscovery: TapTargetView? = null

    override fun showNextFeatureDiscovery() {
        if (!settings.isFeatureDiscovered(FEATURE_TOOL_SHARE) && canShowFeatureDiscovery(FEATURE_TOOL_SHARE)) {
            dispatchDelayedFeatureDiscovery(FEATURE_TOOL_SHARE, false, 2000)
            return
        }

        super.showNextFeatureDiscovery()
    }

    override fun canShowFeatureDiscovery(feature: String) = when (feature) {
        FEATURE_TOOL_SHARE -> toolbar != null && shareMenuItem?.isVisible == true && hasShareLinkUri()
        else -> super.canShowFeatureDiscovery(feature)
    }

    override fun onShowFeatureDiscovery(feature: String, force: Boolean) {
        // dispatch specific feature discovery
        when (feature) {
            FEATURE_TOOL_SHARE -> {
                if (toolbar?.findViewById<View?>(R.id.action_share) != null) {
                    // purge any pending feature discovery triggers since we are showing feature discovery now
                    purgeQueuedFeatureDiscovery(FEATURE_TOOL_SHARE)

                    // show language settings feature discovery
                    val target = TapTarget.forToolbarMenuItem(
                        toolbar, R.id.action_share,
                        getString(R.string.feature_discovery_title_share_tool),
                        getString(R.string.feature_discovery_desc_share_tool)
                    )
                    featureDiscovery = TapTargetView.showFor(this, target, ShareToolFeatureDiscoveryListener())
                    featureDiscoveryActive = feature
                } else {
                    // TODO: we currently don't (can't?) distinguish between when the menu item doesn't exist and when
                    // TODO: the menu item just hasn't been drawn yet.

                    // the toolbar action isn't available yet.
                    // re-attempt this feature discovery on the next frame iteration.
                    dispatchDelayedFeatureDiscovery(feature, force, 17)
                }
            }
            else -> super.onShowFeatureDiscovery(feature, force)
        }
    }

    override fun isFeatureDiscoveryVisible() = featureDiscovery != null || super.isFeatureDiscoveryVisible()

    internal inner class ShareToolFeatureDiscoveryListener : TapTargetView.Listener() {
        override fun onTargetClick(view: TapTargetView) {
            super.onTargetClick(view)
            shareCurrentTool()
        }

        override fun onOuterCircleClick(view: TapTargetView) = onTargetCancel(view)
        override fun onTargetDismissed(view: TapTargetView, userInitiated: Boolean) {
            super.onTargetDismissed(view, userInitiated)
            featureDiscovery = featureDiscovery?.takeUnless { it === view }

            if (userInitiated) {
                settings.setFeatureDiscovered(FEATURE_TOOL_SHARE)
                featureDiscoveryActive = null
                showNextFeatureDiscovery()
            }
        }
    }
    // endregion Feature Discovery

    protected fun trackToolOpen(tool: String) {
        eventBus.post(ToolUsedEvent(tool))

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
    }
}
