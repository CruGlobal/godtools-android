package org.cru.godtools.base.tool.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.eventbus.lifecycle.register
import org.ccci.gto.android.common.util.graphics.toHslColor
import org.cru.godtools.base.Settings.Companion.FEATURE_TOOL_OPENED
import org.cru.godtools.base.Settings.Companion.FEATURE_TOOL_SHARE
import org.cru.godtools.base.tool.BR
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.IS_CONNECTED_LIVE_DATA
import org.cru.godtools.base.tool.R
import org.cru.godtools.base.tool.SHORTCUT_LAUNCH
import org.cru.godtools.base.tool.analytics.model.ShareActionEvent
import org.cru.godtools.base.tool.analytics.model.ToolOpenedAnalyticsActionEvent
import org.cru.godtools.base.tool.analytics.model.ToolOpenedViaShortcutAnalyticsActionEvent
import org.cru.godtools.base.tool.databinding.ToolGenericFragmentActivityBinding
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.util.getTypeface
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.util.applyTypefaceSpan
import org.cru.godtools.download.manager.DownloadProgress
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.ToolUsedEvent
import org.cru.godtools.sync.task.ToolSyncTasks
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.navBarColor
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.keynote.godtools.android.db.GodToolsDao

abstract class BaseToolActivity<B : ViewDataBinding>(@LayoutRes contentLayoutId: Int) :
    BaseActivity<B>(contentLayoutId) {
    @Inject
    internal lateinit var dao: GodToolsDao
    @Inject
    protected lateinit var downloadManager: GodToolsDownloadManager

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // process intent and finish activity if it's in an invalid state
        processIntent(intent, savedInstanceState)
        if (!isValidStartState) {
            finish()
            return
        }

        isConnected.observe(this) { if (it) syncTools() }
        setupStatusBar()
        setupFeatureDiscovery()
        eventBus.register(this, this)
    }

    @CallSuper
    override fun onBindingChanged() {
        binding.setVariable(BR.manifest, activeManifestLiveData)
        binding.setVariable(BR.loadingProgress, activeDownloadProgressLiveData)
        binding.setVariable(BR.loadingState, activeToolLoadingStateLiveData)
    }

    override fun onSetupActionBar() {
        super.onSetupActionBar()
        setupToolbar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_tool, menu)
        menu.setupShareMenuItem()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        updateShareMenuItem()

        // invalidate the binding to force it to re-color the updated menu
        // TODO: this is a very brute-force way of forcing a recoloring of menu items.
        //       We should try and figure out a more targeted solution at some point.
        binding.invalidateAll()

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_share -> {
            shareCurrentTool()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    // endregion Lifecycle

    // region Intent parsing
    protected open fun processIntent(intent: Intent?, savedInstanceState: Bundle?) = Unit
    protected open val isValidStartState get() = true
    // endregion Intent parsing

    /**
     * @return The currently active manifest that is a valid supported type for this activity, otherwise return null.
     */
    protected val activeManifest get() = activeManifestLiveData.value

    // region Status Bar / Toolbar logic
    override val toolbar get() = when (val it = binding) {
        is ToolGenericFragmentActivityBinding -> it.appbar
        else -> super.toolbar
    }

    private fun setupStatusBar() {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activeManifestLiveData.observe(this@BaseToolActivity) {
                statusBarColor = it.navBarColor.toHslColor().darken(0.12f).toColorInt()
            }
        }
    }

    private fun setupToolbar() {
        activeManifestLiveData.observe(this) {
            updateToolbarTitle()
            updateShareMenuItem()
        }
    }

    protected open fun updateToolbarTitle() {
        title = activeManifest?.title.orEmpty()
    }
    // endregion Status Bar / Toolbar logic

    // region Share tool logic
    private val _shareMenuItemVisible by lazy { MutableLiveData(shareLinkUri != null) }
    protected open val shareMenuItemVisible: LiveData<Boolean> get() = _shareMenuItemVisible

    private var shareMenuItemObserver: Observer<Boolean>? = null
    protected fun Menu.setupShareMenuItem() {
        shareMenuItemObserver?.let { shareMenuItemVisible.removeObserver(it) }
        shareMenuItemObserver = Observer<Boolean> {
            findItem(R.id.action_share)?.apply {
                isVisible = it
                isEnabled = it
            }
        }.also { shareMenuItemVisible.observe(this@BaseToolActivity, it) }
    }

    protected fun updateShareMenuItem() {
        _shareMenuItemVisible.value = shareLinkUri != null
        showNextFeatureDiscovery()
    }

    protected open val shareLinkTitle get() = activeManifest?.title
    @get:StringRes
    protected open val shareLinkMessageRes get() = R.string.share_general_message
    protected open val shareLinkUri: String? get() = null

    protected fun shareCurrentTool() {
        // short-circuit if we don't have a share tool url
        val shareUrl = shareLinkUri ?: return

        // track the share action
        eventBus.post(ShareActionEvent)
        settings.setFeatureDiscovered(FEATURE_TOOL_SHARE)

        // start the share activity chooser with our share link
        showShareActivityChooser(shareUrl = shareUrl)
    }

    protected fun showShareActivityChooser(
        @StringRes message: Int = shareLinkMessageRes,
        shareUrl: String? = shareLinkUri
    ) {
        if (shareUrl == null) return

        val title = shareLinkTitle
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_tool_subject, title))
            putExtra(Intent.EXTRA_TEXT, getString(message, shareUrl))
        }
        startActivity(Intent.createChooser(intent, getString(R.string.share_tool_title, title)))
    }
    // endregion Share tool logic

    // region Tool state
    enum class LoadingState {
        LOADING, LOADED, NOT_FOUND, INVALID_TYPE, OFFLINE;

        companion object {
            fun determineToolState(
                manifest: Manifest? = null,
                translation: Translation? = null,
                manifestType: Manifest.Type? = null,
                isConnected: Boolean = true,
                isSyncFinished: Boolean = true
            ) = when {
                manifest != null -> when {
                    manifestType != null && manifest.type != manifestType -> INVALID_TYPE
                    else -> LOADED
                }
                isSyncFinished && translation == null -> NOT_FOUND
                !isConnected -> OFFLINE
                else -> LOADING
            }
        }
    }

    protected abstract val activeDownloadProgressLiveData: LiveData<DownloadProgress?>
    protected abstract val activeManifestLiveData: LiveData<Manifest?>
    protected abstract val activeToolLoadingStateLiveData: LiveData<LoadingState>
    // endregion Tool state

    // region Tool sync/download logic
    @Inject
    internal lateinit var toolSyncTasks: ToolSyncTasks
    @Inject
    @Named(IS_CONNECTED_LIVE_DATA)
    internal lateinit var isConnected: LiveData<Boolean>
    protected open val isInitialSyncFinished = MutableLiveData<Boolean>()

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

    // region Content Event Logic
    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun processContentEvent(event: Event) {
        val manifest = activeManifest ?: return
        if (manifest.code != event.tool || manifest.locale != event.locale) return

        checkForManifestEvent(manifest, event)
        if (isFinishing) return
        onContentEvent(event)
    }

    @MainThread
    protected open fun onContentEvent(event: Event) = Unit

    protected open fun checkForManifestEvent(manifest: Manifest, event: Event) {
        if (event.id in manifest.dismissListeners) {
            finish()
            return
        }
    }
    // endregion Content Event Logic

    // region Feature Discovery
    private var featureDiscovery: TapTargetView? = null

    private fun setupFeatureDiscovery() {
        shareMenuItemVisible.observe(this) { showNextFeatureDiscovery() }
    }

    override fun showNextFeatureDiscovery() {
        if (!settings.isFeatureDiscovered(FEATURE_TOOL_SHARE) && canShowFeatureDiscovery(FEATURE_TOOL_SHARE)) {
            dispatchDelayedFeatureDiscovery(FEATURE_TOOL_SHARE, false, 2000)
            return
        }

        super.showNextFeatureDiscovery()
    }

    override fun canShowFeatureDiscovery(feature: String) = when (feature) {
        FEATURE_TOOL_SHARE -> toolbar != null && shareMenuItemVisible.value == true
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
        eventBus.post(ToolOpenedAnalyticsActionEvent(first = !settings.isFeatureDiscovered(FEATURE_TOOL_OPENED)))
        if (intent.isShortcutLaunch) eventBus.post(ToolOpenedViaShortcutAnalyticsActionEvent)

        settings.setFeatureDiscovered(FEATURE_TOOL_OPENED)

        GlobalScope.launch { dao.updateSharesDelta(tool, 1) }
    }

    private val Intent?.isShortcutLaunch get() = this?.getBooleanExtra(SHORTCUT_LAUNCH, false) ?: false

    override fun setTitle(title: CharSequence) =
        super.setTitle(title.applyTypefaceSpan(activeManifest?.getTypeface(this)))
}
