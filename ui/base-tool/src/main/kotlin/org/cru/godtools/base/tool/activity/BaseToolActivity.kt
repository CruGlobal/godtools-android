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
import androidx.lifecycle.asLiveData
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.Ordered
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.ccci.gto.android.common.eventbus.lifecycle.register
import org.ccci.gto.android.common.util.graphics.toHslColor
import org.cru.godtools.base.Settings.Companion.FEATURE_TOOL_OPENED
import org.cru.godtools.base.Settings.Companion.FEATURE_TOOL_SHARE
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.IS_CONNECTED_LIVE_DATA
import org.cru.godtools.base.tool.SHORTCUT_LAUNCH
import org.cru.godtools.base.tool.analytics.model.ShareActionEvent
import org.cru.godtools.base.tool.analytics.model.ToolOpenedAnalyticsActionEvent
import org.cru.godtools.base.tool.analytics.model.ToolOpenedViaShortcutAnalyticsActionEvent
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.service.FollowupService
import org.cru.godtools.base.tool.ui.share.ShareBottomSheetDialogFragment
import org.cru.godtools.base.tool.ui.share.model.DefaultShareItem
import org.cru.godtools.base.tool.ui.share.model.ShareItem
import org.cru.godtools.base.tool.ui.shareable.model.ShareableImageShareItem
import org.cru.godtools.base.tool.ui.util.getTypeface
import org.cru.godtools.base.ui.activity.BaseBindingActivity
import org.cru.godtools.base.ui.util.applyTypefaceSpan
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.downloadmanager.GodToolsDownloadManager
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.ToolUsedEvent
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.navBarColor
import org.cru.godtools.shared.tool.parser.model.shareable.ShareableImage
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.tool.BR
import org.cru.godtools.tool.R
import org.cru.godtools.tool.databinding.ToolGenericFragmentActivityBinding
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class BaseToolActivity<B : ViewDataBinding>(@LayoutRes contentLayoutId: Int) :
    BaseBindingActivity<B>(contentLayoutId) {
    @Inject
    lateinit var downloadManager: GodToolsDownloadManager
    @Inject
    @Named(IS_CONNECTED_LIVE_DATA)
    internal lateinit var isConnected: LiveData<Boolean>
    @Inject
    internal lateinit var toolsRepository: ToolsRepository

    protected abstract val viewModel: BaseToolRendererViewModel

    // Inject the FollowupService to ensure it is running to capture any followup forms
    @Inject
    internal lateinit var followupService: FollowupService

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // process intent and finish activity if it's in an invalid state
        intent?.let { processIntent(it, savedInstanceState) }
        if (!isValidStartState) {
            finish()
            return
        }

        setupToolSync()
        setupStatusBar()
        setupFeatureDiscovery()
        eventBus.register(this, this)
    }

    @CallSuper
    override fun onBindingChanged() {
        binding.setVariable(BR.manifest, viewModel.manifest.asLiveData())
        binding.setVariable(BR.loadingProgress, viewModel.downloadProgress.asLiveData())
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
        // Tool Share feature discovery is dependent on the toolbar being available, so we trigger it now since the
        // toolbar will exist
        showNextFeatureDiscovery()

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
    protected open fun processIntent(intent: Intent, savedInstanceState: Bundle?) = Unit
    protected open val isValidStartState get() = true
    // endregion Intent parsing

    /**
     * @return The currently active manifest that is a valid supported type for this activity, otherwise return null.
     */
    protected val activeManifest get() = viewModel.manifest.value

    // region Status Bar / Toolbar logic
    override val toolbar get() = when (val it = binding) {
        is ToolGenericFragmentActivityBinding -> it.appbar
        else -> super.toolbar
    }

    private fun setupStatusBar() {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            viewModel.manifest.asLiveData().observe(this@BaseToolActivity) {
                statusBarColor = it.navBarColor.toHslColor().darken(0.12f).toColorInt()
            }
        }
    }

    private fun setupToolbar() {
        title = ""
    }
    // endregion Status Bar / Toolbar logic

    // region Share tool logic
    protected open val shareMenuItemVisible by lazy { shareLinkUriLiveData.map { it != null } }

    protected open val shareLinkTitle get() = activeManifest?.title
    @get:StringRes
    protected open val shareLinkMessageRes get() = R.string.share_tool_message
    protected open val shareLinkUriLiveData = emptyLiveData<String>()
    private val shareLinkUri get() = shareLinkUriLiveData.value

    private fun Menu.setupShareMenuItem() {
        findItem(R.id.action_share)?.let { item ->
            shareMenuItemVisible.observe(this@BaseToolActivity, item) {
                isVisible = it
                isEnabled = it
            }
        }
    }

    fun shareCurrentTool() {
        val shareItems = getShareItems().sortedWith(Ordered.COMPARATOR)
        if (shareItems.isEmpty()) return

        // track the share action
        eventBus.post(ShareActionEvent)
        settings.setFeatureDiscovered(FEATURE_TOOL_SHARE)

        // launch the appropriate Share Dialog based on shareItems
        when (shareItems.size) {
            1 -> shareItems.first().triggerAction(this)
            else -> ShareBottomSheetDialogFragment(shareItems).show(supportFragmentManager, null)
        }
    }

    private fun getShareItems(): Collection<ShareItem> = buildList {
        buildShareIntent()?.let { add(DefaultShareItem(it)) }
        addAll(getShareableShareItems())
    }.filter { it.isValid }

    @Inject
    internal lateinit var shareableShareItemFactory: ShareableImageShareItem.Factory

    protected open fun getShareableShareItems() = activeManifest?.shareables?.filterIsInstance<ShareableImage>()
        ?.map { shareableShareItemFactory.create(it) }.orEmpty()

    private fun buildShareIntent(
        title: String? = shareLinkTitle,
        @StringRes message: Int = shareLinkMessageRes,
        shareUrl: String? = shareLinkUri
    ) = shareUrl?.let {
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title.orEmpty())
            putExtra(Intent.EXTRA_TEXT, getString(message, shareUrl))
        }
    }

    protected fun showShareActivityChooser(
        title: String? = shareLinkTitle,
        @StringRes message: Int = shareLinkMessageRes,
        shareUrl: String? = shareLinkUri
    ) {
        val intent = buildShareIntent(title, message, shareUrl) ?: return
        DefaultShareItem(intent).triggerAction(this)
    }
    // endregion Share tool logic

    // region Tool state
    enum class LoadingState {
        LOADING,
        LOADED,
        NOT_FOUND,
        INVALID_TYPE,
        OFFLINE;

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

    protected abstract val activeToolLoadingStateLiveData: LiveData<LoadingState>
    // endregion Tool state

    // region Tool sync/download logic
    @Inject
    internal lateinit var syncService: GodToolsSyncService
    protected open val isInitialSyncFinished = MutableStateFlow(false)

    protected abstract val toolsToDownload: StateFlow<List<String>>
    protected abstract val localesToDownload: StateFlow<List<Locale>>

    private fun setupToolSync() {
        combine(toolsToDownload, localesToDownload) { tools, locales -> Pair(tools, locales) }
            .flowWithLifecycle(lifecycle)
            .onEach { downloadTranslations(it.first, it.second) }
            .launchIn(lifecycleScope)

        isConnected.observe(this) { if (it) syncTools() }
    }

    private fun syncTools(tools: List<String> = toolsToDownload.value) = lifecycleScope.launch {
        try {
            coroutineScope {
                tools.forEach { launch { syncService.syncTool(it) } }
            }
            isInitialSyncFinished.value = true
        } catch (ignored: IOException) {
        }
        downloadTranslations()
    }

    private fun downloadTranslations(
        tools: List<String> = toolsToDownload.value,
        locales: List<Locale> = localesToDownload.value
    ) = tools.forEach { t -> locales.forEach { l -> downloadManager.downloadLatestPublishedTranslationAsync(t, l) } }
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
                        toolbar,
                        R.id.action_share,
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

    protected fun trackToolOpen(tool: String, type: Manifest.Type? = null) {
        eventBus.post(ToolUsedEvent(tool))
        eventBus.post(ToolOpenedAnalyticsActionEvent(tool, type, !settings.isFeatureDiscovered(FEATURE_TOOL_OPENED)))
        if (intent.isShortcutLaunch) eventBus.post(ToolOpenedViaShortcutAnalyticsActionEvent)

        if (type == Manifest.Type.ARTICLE || type == Manifest.Type.CYOA || type == Manifest.Type.TRACT) {
            settings.setFeatureDiscovered(FEATURE_TOOL_OPENED)
        }

        lifecycleScope.launch {
            toolsRepository.updateToolViews(tool, 1)
            @Suppress("DeferredResultUnused")
            syncService.syncToolSharesAsync()
        }
    }

    private val Intent?.isShortcutLaunch get() = this?.getBooleanExtra(SHORTCUT_LAUNCH, false) ?: false

    override fun setTitle(title: CharSequence) =
        super.setTitle(title.applyTypefaceSpan(activeManifest?.getTypeface(this)))
}
