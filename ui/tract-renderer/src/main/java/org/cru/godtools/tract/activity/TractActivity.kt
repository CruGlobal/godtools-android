package org.cru.godtools.tract.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.google.android.instantapps.InstantApps
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.fragment.app.showAllowingStateLoss
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.notNull
import org.ccci.gto.android.common.androidx.lifecycle.observeOnce
import org.ccci.gto.android.common.util.LocaleUtils
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_LIVE_SHARE
import org.cru.godtools.base.URI_SHARE_BASE
import org.cru.godtools.base.tool.EXTRA_SHOW_TIPS
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivity
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.tool.model.backgroundColor
import org.cru.godtools.tool.model.tips.Tip
import org.cru.godtools.tool.model.tract.Card
import org.cru.godtools.tool.model.tract.Modal
import org.cru.godtools.tool.model.tract.TractPage
import org.cru.godtools.tract.PARAM_LIVE_SHARE_STREAM
import org.cru.godtools.tract.PARAM_PARALLEL_LANGUAGE
import org.cru.godtools.tract.PARAM_PRIMARY_LANGUAGE
import org.cru.godtools.tract.PARAM_USE_DEVICE_LANGUAGE
import org.cru.godtools.tract.R
import org.cru.godtools.tract.adapter.ManifestPagerAdapter
import org.cru.godtools.tract.analytics.model.ShareScreenEngagedActionEvent
import org.cru.godtools.tract.analytics.model.ShareScreenOpenedActionEvent
import org.cru.godtools.tract.analytics.model.TractPageAnalyticsScreenEvent
import org.cru.godtools.tract.databinding.TractActivityBinding
import org.cru.godtools.tract.liveshare.State
import org.cru.godtools.tract.liveshare.TractPublisherController
import org.cru.godtools.tract.liveshare.TractSubscriberController
import org.cru.godtools.tract.service.FollowupService
import org.cru.godtools.tract.ui.liveshare.LiveShareExitDialogFragment
import org.cru.godtools.tract.ui.liveshare.LiveShareStartingDialogFragment
import org.cru.godtools.tract.ui.tips.TipBottomSheetDialogFragment
import org.cru.godtools.tract.util.isTractDeepLink
import org.cru.godtools.tract.util.loadAnimation
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.TutorialActivityResultContract

private const val EXTRA_INITIAL_PAGE = "org.cru.godtools.tract.activity.TractActivity.INITIAL_PAGE"

@AndroidEntryPoint
class TractActivity :
    MultiLanguageToolActivity<TractActivityBinding>(R.layout.tract_activity),
    ManifestPagerAdapter.Callbacks,
    TipBottomSheetDialogFragment.Callbacks {
    private val savedState: TractActivitySavedState by viewModels()

    // Inject the FollowupService to ensure it is running to capture any followup forms
    @Inject
    internal lateinit var followupService: FollowupService

    private val showTips get() = intent?.getBooleanExtra(EXTRA_SHOW_TIPS, false) ?: false

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return

        // restore any saved state
        savedInstanceState?.run {
            initialPage = getInt(EXTRA_INITIAL_PAGE, initialPage)
        }

        // track this view
        if (savedInstanceState == null) dataModel.toolCode.value?.let { trackToolOpen(it) }

        attachLiveSharePublishExitBehavior()
        startLiveShareSubscriberIfNecessary(savedInstanceState)
    }

    override fun onBindingChanged() {
        super.onBindingChanged()
        setupBinding()
    }

    override fun onContentChanged() {
        super.onContentChanged()
        setupBackground()
        setupPager()
    }

    @CallSuper
    override fun onSetupActionBar() {
        super.onSetupActionBar()
        setupActionBarTitle()
        if (InstantApps.isInstantApp(this)) toolbar.setNavigationIcon(R.drawable.ic_close)
    }

    override fun onCreateOptionsMenu(menu: Menu) = super.onCreateOptionsMenu(menu).also {
        menu.removeItem(R.id.action_share)
        menuInflater.inflate(R.menu.activity_tract, menu)
        menuInflater.inflate(R.menu.activity_tract_live_share, menu)
        menu.setupLiveShareMenuItemVisibility()
        menu.setupShareMenuItem()

        // Adjust visibility of menu items
        menu.findItem(R.id.action_install)?.isVisible = InstantApps.isInstantApp(this)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when {
        item.itemId == R.id.action_install -> {
            InstantApps.showInstallPrompt(this, -1, "instantapp")
            true
        }
        item.itemId == R.id.action_live_share_publish -> {
            publisherController.started = true
            shareLiveShareLink()
            true
        }
        // override default share action to support live share submenu
        item.itemId == R.id.action_share -> true
        item.itemId == R.id.action_share_tool -> {
            shareCurrentTool()
            true
        }
        // handle close button if this is an instant app
        item.itemId == android.R.id.home && InstantApps.isInstantApp(this) -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDismissTip() = trackTractPage()

    override fun onContentEvent(event: Event) {
        checkForPageEvent(event)
        propagateEventToPage(event)
    }

    override fun onUpdateActiveCard(page: TractPage, card: Card?) {
        trackTractPage(page, card)
        sendLiveShareNavigationEvent(page, card)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_INITIAL_PAGE, initialPage)
    }
    // endregion Lifecycle

    // region Intent Processing
    override fun processIntent(intent: Intent?, savedInstanceState: Bundle?) {
        super.processIntent(intent, savedInstanceState)
        val data = intent?.data
        if (intent?.action == Intent.ACTION_VIEW && data?.isTractDeepLink() == true) {
            dataModel.toolCode.value = data.deepLinkTool
            val (primary, parallel) = data.deepLinkLanguages
            dataModel.primaryLocales.value = primary
            dataModel.parallelLocales.value = parallel
            if (savedInstanceState == null) {
                dataModel.setActiveLocale(data.deepLinkSelectedLanguage)
                data.deepLinkPage?.let { initialPage = it }
            }
        }
    }

    @VisibleForTesting
    internal val Uri.deepLinkSelectedLanguage get() = Locale.forLanguageTag(pathSegments[0])
    @VisibleForTesting
    internal val Uri.deepLinkTool get() = pathSegments[1]
    @VisibleForTesting
    internal val Uri.deepLinkPage get() = pathSegments.getOrNull(2)?.toIntOrNull()

    @VisibleForTesting
    internal val Uri.deepLinkLanguages: Pair<List<Locale>, List<Locale>> get() {
        val primary = LinkedHashSet<Locale>()
        val parallel = LinkedHashSet<Locale>()
        val selected = deepLinkSelectedLanguage

        if (getQueryParameter(PARAM_USE_DEVICE_LANGUAGE)?.isNotEmpty() == true) {
            primary += LocaleUtils.getFallbacks(Locale.getDefault())
        }
        primary += LocaleUtils.getFallbacks(*extractLanguagesFromDeepLinkParam(PARAM_PRIMARY_LANGUAGE).toTypedArray())
        parallel += LocaleUtils.getFallbacks(*extractLanguagesFromDeepLinkParam(PARAM_PARALLEL_LANGUAGE).toTypedArray())

        if (selected !in primary && selected !in parallel) primary += LocaleUtils.getFallbacks(selected)

        return Pair(primary.toList(), parallel.toList())
    }

    private fun Uri.extractLanguagesFromDeepLinkParam(param: String) = getQueryParameters(param)
        .flatMap { it.split(",") }
        .map { it.trim() }.filterNot { it.isEmpty() }
        .map { Locale.forLanguageTag(it) }
    // endregion Intent Processing

    // region UI
    override val toolbar get() = binding.appbar
    override val languageToggle get() = binding.languageToggle

    private fun setupBinding() {
        binding.activeLocale = dataModel.activeLocale
        binding.visibleLocales = dataModel.visibleLocales
    }

    private fun setupActionBarTitle() {
        dataModel.activeLocale.combineWith(dataModel.visibleLocales) { active, locales ->
            locales.isEmpty() || (locales.size < 2 && locales.contains(active))
        }.observe(this) { supportActionBar?.setDisplayShowTitleEnabled(it) }
    }

    private fun setupBackground() {
        dataModel.activeManifest.observe(this) { window.decorView.setBackgroundColor(it.backgroundColor) }
    }

    // region Tool Pager
    @Inject
    internal lateinit var pagerAdapterFactory: ManifestPagerAdapter.Factory
    private val pager get() = binding.pages
    private val pagerAdapter by lazy {
        pagerAdapterFactory.create(this, toolState.toolState).also { adapter ->
            adapter.callbacks = this
            adapter.showTips = showTips
            dataModel.activeManifest.observe(this) { manifest ->
                val sameLocale = adapter.manifest?.locale == manifest?.locale
                adapter.manifest = manifest

                // trigger analytics & live share publisher events if the locale was changed
                if (!sameLocale) {
                    adapter.primaryItem?.binding?.controller?.let {
                        val page = it.model ?: return@let
                        val card = it.activeCard
                        trackTractPage(page, card)
                        sendLiveShareNavigationEvent(page, card)
                    }
                }
            }
        }
    }
    private var initialPage = 0

    private fun setupPager() {
        pager.adapter = pagerAdapter
        dataModel.visibleLocales.observe(this) {
            pager.layoutDirection = TextUtils.getLayoutDirectionFromLocale(it.firstOrNull())
        }

        if (initialPage >= 0) dataModel.activeManifest.notNull().observeOnce(this) {
            if (initialPage < 0) return@observeOnce

            // HACK: set the manifest in the pager adapter to ensure setCurrentItem works.
            //       This is normally handled by the pager adapter observer.
            pagerAdapter.manifest = it
            pager.setCurrentItem(initialPage, false)
            initialPage = -1
        }
    }

    private fun checkForPageEvent(event: Event) {
        activeManifest?.tractPages?.firstOrNull { it.listeners.contains(event.id) }?.let { goToPage(it.position) }
    }

    private fun propagateEventToPage(event: Event) {
        pagerAdapter.onContentEvent(event)
    }

    // region ManifestPagerAdapter.Callbacks
    override fun goToPage(position: Int) {
        pager.currentItem = position
    }

    override fun showModal(modal: Modal) = startModalActivity(modal)
    override fun showTip(tip: Tip) {
        TipBottomSheetDialogFragment.create(tip)?.show(supportFragmentManager, null)
    }
    // endregion ManifestPagerAdapter.Callbacks
    // endregion Tool Pager
    // endregion UI

    private fun trackTractPage(
        page: TractPage? = pagerAdapter.primaryItem?.binding?.controller?.model,
        card: Card? = pagerAdapter.primaryItem?.binding?.controller?.activeCard
    ) {
        page?.let { eventBus.post(TractPageAnalyticsScreenEvent(page, card)) }
    }

    // region Share Menu Logic
    override val shareMenuItemVisible by lazy {
        activeManifestLiveData.combineWith(subscriberController.state) { manifest, subscriberState ->
            manifest != null && subscriberState == State.Off && !showTips
        }
    }

    override val shareLinkUri get() = buildShareLink()?.build()?.toString()
    private fun buildShareLink(): Uri.Builder? {
        val manifest = activeManifest ?: return null
        val tool = manifest.code ?: return null
        val locale = manifest.locale ?: return null
        return URI_SHARE_BASE.buildUpon()
            .appendEncodedPath(locale.toLanguageTag().lowercase(Locale.ENGLISH))
            .appendPath(tool)
            .apply { if (pager.currentItem > 0) appendPath(pager.currentItem.toString()) }
            .appendQueryParameter("icid", "gtshare")
    }
    // endregion Share Menu Logic

    // region Live Share Logic
    private val publisherController: TractPublisherController by viewModels()
    private val subscriberController: TractSubscriberController by viewModels()
    private val liveShareTutorialLauncher = registerForActivityResult(TutorialActivityResultContract()) {
        when (it) {
            RESULT_CANCELED -> publisherController.started = false
            else -> {
                savedState.liveShareTutorialShown = true
                settings.setFeatureDiscovered("$FEATURE_TUTORIAL_LIVE_SHARE${dataModel.toolCode.value}")
                shareLiveShareLink()
            }
        }
    }

    private val liveShareState: LiveData<Pair<State, State>> by lazy {
        publisherController.state.combineWith(subscriberController.state) { pState, sState -> pState to sState }
    }
    private var liveShareMenuObserver: Observer<Pair<State, State>>? = null
    private fun Menu.setupLiveShareMenuItemVisibility() {
        liveShareMenuObserver?.let { liveShareState.removeObserver(it) }

        val liveShareItem = findItem(R.id.action_live_share_active)
        liveShareItem?.loadAnimation(this@TractActivity, R.raw.anim_tract_live_share)
        liveShareMenuObserver = Observer<Pair<State, State>> { (publisherState, subscriberState) ->
            liveShareItem?.isVisible = publisherState == State.On || subscriberState == State.On
        }.also { liveShareState.observe(this@TractActivity, it) }
    }

    internal fun shareLiveShareLink() {
        when {
            !savedState.liveShareTutorialShown &&
                settings.getFeatureDiscoveredCount("$FEATURE_TUTORIAL_LIVE_SHARE${dataModel.toolCode.value}") < 3 ->
                liveShareTutorialLauncher.launch(PageSet.LIVE_SHARE)
            publisherController.publisherInfo.value == null ->
                LiveShareStartingDialogFragment().showAllowingStateLoss(supportFragmentManager, null)
            else -> {
                val subscriberId = publisherController.publisherInfo.value?.subscriberChannelId ?: return
                val shareUrl = (buildShareLink() ?: return)
                    .apply {
                        dataModel.primaryLocales.value?.takeUnless { it.isEmpty() }
                            ?.joinToString(",") { it.toLanguageTag() }
                            ?.let { appendQueryParameter(PARAM_PRIMARY_LANGUAGE, it) }
                        dataModel.parallelLocales.value?.takeUnless { it.isEmpty() }
                            ?.joinToString(",") { it.toLanguageTag() }
                            ?.let { appendQueryParameter(PARAM_PARALLEL_LANGUAGE, it) }
                    }
                    .appendQueryParameter(PARAM_LIVE_SHARE_STREAM, subscriberId)
                    .build().toString()
                eventBus.post(ShareScreenEngagedActionEvent)
                showShareActivityChooser(message = R.string.share_tool_message_tract_live_share, shareUrl = shareUrl)
            }
        }
    }

    private fun sendLiveShareNavigationEvent(page: TractPage, card: Card?) {
        publisherController.sendNavigationEvent(
            NavigationEvent(page.manifest.code, page.manifest.locale, page.position, card?.position)
        )
    }

    private fun startLiveShareSubscriberIfNecessary(savedInstanceState: Bundle?) {
        val streamId = intent?.data?.getQueryParameter(PARAM_LIVE_SHARE_STREAM) ?: return

        subscriberController.channelId = streamId
        subscriberController.receivedEvent.notNull().distinctUntilChanged()
            .observe(this) { navigateToLiveShareEvent(it) }
        if (savedInstanceState == null) eventBus.post(ShareScreenOpenedActionEvent)
    }

    private fun navigateToLiveShareEvent(event: NavigationEvent?) {
        if (event == null) return
        event.locale?.takeUnless { it == dataModel.activeLocale.value }?.let {
            dataModel.toolCode.value?.let { tool -> downloadManager.downloadLatestPublishedTranslationAsync(tool, it) }
            dataModel.setActiveLocale(it)
        }
        event.page?.let { goToPage(it) }
        eventBus.post(event)
    }

    // region Exit Live Share Publishing
    private fun attachLiveSharePublishExitBehavior() {
        onBackPressedDispatcher.addCallback(this, false) { showExitLiveShareDialog() }
            .also { c -> publisherController.state.observe(this) { c.isEnabled = it == State.On } }
    }

    private fun showExitLiveShareDialog() {
        LiveShareExitDialogFragment().show(supportFragmentManager, null)
    }
    // endregion Exit Live Share Publishing
    // endregion Live Share Logic

    override fun supportNavigateUpTo(upIntent: Intent) {
        if (publisherController.state.value == State.On) {
            showExitLiveShareDialog()
            return
        }
        super.supportNavigateUpTo(upIntent)
    }
}
