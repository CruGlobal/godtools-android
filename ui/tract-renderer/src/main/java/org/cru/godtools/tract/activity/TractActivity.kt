package org.cru.godtools.tract.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
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
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.fragment.app.showAllowingStateLoss
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.notNull
import org.ccci.gto.android.common.androidx.lifecycle.observeOnce
import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.ccci.gto.android.common.compat.view.ViewCompat
import org.ccci.gto.android.common.util.LocaleUtils
import org.ccci.gto.android.common.util.graphics.toHsvColor
import org.ccci.gto.android.common.util.os.getLocaleArray
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.base.EXTRA_LANGUAGES
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_LIVE_SHARE
import org.cru.godtools.base.URI_SHARE_BASE
import org.cru.godtools.base.model.Event
import org.cru.godtools.base.tool.EXTRA_SHOW_TIPS
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.cru.godtools.tract.PARAM_LIVE_SHARE_STREAM
import org.cru.godtools.tract.PARAM_PARALLEL_LANGUAGE
import org.cru.godtools.tract.PARAM_PRIMARY_LANGUAGE
import org.cru.godtools.tract.PARAM_USE_DEVICE_LANGUAGE
import org.cru.godtools.tract.R
import org.cru.godtools.tract.adapter.ManifestPagerAdapter
import org.cru.godtools.tract.analytics.model.ShareScreenEngagedActionEvent
import org.cru.godtools.tract.analytics.model.ShareScreenOpenedActionEvent
import org.cru.godtools.tract.analytics.model.ToggleLanguageAnalyticsActionEvent
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
import org.cru.godtools.tutorial.activity.buildTutorialActivityIntent
import org.cru.godtools.xml.model.backgroundColor
import org.cru.godtools.xml.model.navBarColor
import org.cru.godtools.xml.model.navBarControlColor
import org.cru.godtools.xml.model.tips.Tip
import org.cru.godtools.xml.model.tract.Card
import org.cru.godtools.xml.model.tract.Modal
import org.cru.godtools.xml.model.tract.TractPage

private const val EXTRA_INITIAL_PAGE = "org.cru.godtools.tract.activity.TractActivity.INITIAL_PAGE"

private const val REQUEST_LIVE_SHARE_TUTORIAL = 100

@AndroidEntryPoint
class TractActivity :
    BaseToolActivity<TractActivityBinding>(R.layout.tract_activity),
    TabLayout.OnTabSelectedListener,
    ManifestPagerAdapter.Callbacks,
    TipBottomSheetDialogFragment.Callbacks {
    // Inject the FollowupService to ensure it is running to capture any followup forms
    @Inject
    internal lateinit var followupService: FollowupService

    private val showTips get() = intent?.getBooleanExtra(EXTRA_SHOW_TIPS, false) ?: false

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        processIntent(intent, savedInstanceState)

        // finish now if this activity is in an invalid start state
        if (!validStartState()) {
            finish()
            return
        }

        // restore any saved state
        savedInstanceState?.run {
            initialPage = getInt(EXTRA_INITIAL_PAGE, initialPage)
        }

        // track this view
        if (savedInstanceState == null) dataModel.tool.value?.let { trackToolOpen(it) }

        setupDataModel()
        setupActiveTranslationManagement()
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
        setupLanguageToggle()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = when (requestCode) {
        REQUEST_LIVE_SHARE_TUTORIAL -> when (resultCode) {
            Activity.RESULT_OK -> {
                dataModel.liveShareTutorialShown = true
                settings.setFeatureDiscovered("$FEATURE_TUTORIAL_LIVE_SHARE${dataModel.tool.value}")
                shareLiveShareLink()
            }
            Activity.RESULT_CANCELED -> publisherController.started = false
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
        else -> super.onActivityResult(requestCode, resultCode, data)
    }

    @CallSuper
    override fun onUpdateActiveManifest() {
        super.onUpdateActiveManifest()
        showNextFeatureDiscovery()
    }

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
    private fun processIntent(intent: Intent?, savedInstanceState: Bundle?) {
        val data = intent?.data
        val extras = intent?.extras
        if (intent?.action == Intent.ACTION_VIEW && data?.isTractDeepLink(this) == true) {
            dataModel.tool.value = data.deepLinkTool
            val (primary, parallel) = data.deepLinkLanguages
            dataModel.primaryLocales.value = primary
            dataModel.parallelLocales.value = parallel
            if (savedInstanceState == null) {
                dataModel.setActiveLocale(data.deepLinkSelectedLanguage)
                data.deepLinkPage?.let { initialPage = it }
            }
        } else if (extras != null) {
            dataModel.tool.value = extras.getString(EXTRA_TOOL, dataModel.tool.value)
            val languages = extras.getLocaleArray(EXTRA_LANGUAGES)?.filterNotNull().orEmpty()
            dataModel.primaryLocales.value = if (languages.isNotEmpty()) languages.subList(0, 1) else emptyList()
            dataModel.parallelLocales.value =
                if (languages.size > 1) languages.subList(1, languages.size) else emptyList()
        } else {
            dataModel.tool.value = null
        }
    }

    @VisibleForTesting
    internal val Uri.deepLinkSelectedLanguage get() = LocaleCompat.forLanguageTag(pathSegments[0])
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
        .map { LocaleCompat.forLanguageTag(it) }
    // endregion Intent Processing

    private fun validStartState() = dataModel.tool.value != null &&
        (!dataModel.primaryLocales.value.isNullOrEmpty() || !dataModel.parallelLocales.value.isNullOrEmpty())

    // region Data Model
    private val dataModel: TractActivityDataModel by viewModels()
    private fun setupDataModel() {
        dataModel.activeManifest.observe(this) { onUpdateActiveManifest() }
    }
    // endregion Data Model

    // region UI
    override val toolbar get() = binding.appbar

    override val activeDownloadProgressLiveData get() = dataModel.downloadProgress

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

    // region Language Toggle
    private lateinit var languageToggleController: LanguageToggleController

    private fun setupLanguageToggle() {
        ViewCompat.setClipToOutline(binding.languageToggle, true)
        dataModel.activeManifest.observe(this) { manifest ->
            // determine colors for the language toggle
            val controlColor = manifest.navBarControlColor
            var selectedColor = manifest.navBarColor
            if (Color.alpha(selectedColor) < 255) {
                // XXX: the expected behavior is to support transparent text. But we currently don't support
                //      transparent text, so pick white or black based on the control color
                selectedColor = if (controlColor.toHsvColor().value > 0.6) Color.BLACK else Color.WHITE
            }

            // update colors for tab text
            binding.languageToggle.setTabTextColors(controlColor, selectedColor)
        }

        languageToggleController = LanguageToggleController(binding.languageToggle).also { controller ->
            dataModel.activeLocale.observe(this) { controller.activeLocale = it }
            dataModel.activeManifest.observe(this) { controller.activeManifest = it }
            dataModel.visibleLocales.observe(this) { controller.locales = it }
            dataModel.languages.observe(this) { controller.languages = it }
        }

        binding.languageToggle.addOnTabSelectedListener(this)
    }

    // region TabLayout.OnTabSelectedListener
    override fun onTabSelected(tab: TabLayout.Tab) {
        if (languageToggleController.isUpdatingTabs) return
        val locale = tab.tag as? Locale ?: return
        eventBus.post(ToggleLanguageAnalyticsActionEvent(dataModel.tool.value, locale))
        dataModel.setActiveLocale(locale)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) = Unit
    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
    // endregion TabLayout.OnTabSelectedListener
    // endregion Language Toggle

    // region Tool Pager
    @Inject
    internal lateinit var pagerAdapterFactory: ManifestPagerAdapter.Factory
    private val pager get() = binding.pages
    private val pagerAdapter by lazy {
        pagerAdapterFactory.create(this).also { adapter ->
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
    override fun showTip(tip: Tip) = TipBottomSheetDialogFragment(tip).show(supportFragmentManager, null)
    // endregion ManifestPagerAdapter.Callbacks
    // endregion Tool Pager
    // endregion UI

    override fun cacheTools() {
        dataModel.tool.value?.let { tool ->
            dataModel.locales.value?.forEach { downloadManager.downloadLatestPublishedTranslationAsync(tool, it) }
        }
    }

    private fun trackTractPage(
        page: TractPage? = pagerAdapter.primaryItem?.binding?.controller?.model,
        card: Card? = pagerAdapter.primaryItem?.binding?.controller?.activeCard
    ) {
        page?.let { eventBus.post(TractPageAnalyticsScreenEvent(page, card)) }
    }

    // region Active Translation management
    override val activeManifestLiveData get() = dataModel.activeManifest
    override val activeToolStateLiveData get() = dataModel.activeState

    private fun setupActiveTranslationManagement() {
        isInitialSyncFinished.observe(this) { if (it) dataModel.isInitialSyncFinished.value = true }
        dataModel.locales.map { it.firstOrNull() }.notNull().observeOnce(this) {
            if (dataModel.activeLocale.value == null) dataModel.setActiveLocale(it)
        }

        dataModel.availableLocales.observe(this) {
            updateActiveLocaleToAvailableLocaleIfNecessary(availableLocales = it)
        }
        dataModel.activeState.observe(this) { updateActiveLocaleToAvailableLocaleIfNecessary(activeState = it) }
        dataModel.state.observe(this) { updateActiveLocaleToAvailableLocaleIfNecessary(state = it) }
    }

    private fun updateActiveLocaleToAvailableLocaleIfNecessary(
        activeState: ToolState? = dataModel.activeState.value,
        availableLocales: List<Locale> = dataModel.availableLocales.value.orEmpty(),
        state: Map<Locale, ToolState> = dataModel.state.value.orEmpty()
    ) {
        when (activeState) {
            // update the active language if the current active language is not found, invalid, or offline
            ToolState.NOT_FOUND,
            ToolState.INVALID_TYPE,
            ToolState.OFFLINE -> availableLocales.firstOrNull {
                state[it] != ToolState.NOT_FOUND && state[it] != ToolState.INVALID_TYPE &&
                    state[it] != ToolState.OFFLINE
            }?.let { dataModel.setActiveLocale(it) }
        }
    }
    // endregion Active Translation management

    // region Share Menu Logic
    override val shareMenuItemVisible by lazy {
        activeManifestLiveData.combineWith(subscriberController.state) { manifest, subscriberState ->
            manifest != null && subscriberState == State.Off && !showTips
        }
    }

    override val shareLinkUri get() = buildShareLink()?.build()?.toString()
    private fun buildShareLink() = activeManifest?.let {
        URI_SHARE_BASE.buildUpon()
            .appendEncodedPath(LocaleCompat.toLanguageTag(it.locale).toLowerCase(Locale.ENGLISH))
            .appendPath(it.code)
            .apply { if (pager.currentItem > 0) appendPath(pager.currentItem.toString()) }
            .appendQueryParameter("icid", "gtshare")
    }
    // endregion Share Menu Logic

    // region Live Share Logic
    private val publisherController: TractPublisherController by viewModels()
    private val subscriberController: TractSubscriberController by viewModels()

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
            !dataModel.liveShareTutorialShown &&
                settings.getFeatureDiscoveredCount("$FEATURE_TUTORIAL_LIVE_SHARE${dataModel.tool.value}") < 3 ->
                startActivityForResult(buildTutorialActivityIntent(PageSet.LIVE_SHARE), REQUEST_LIVE_SHARE_TUTORIAL)
            publisherController.publisherInfo.value == null ->
                LiveShareStartingDialogFragment().showAllowingStateLoss(supportFragmentManager, null)
            else -> {
                val subscriberId = publisherController.publisherInfo.value?.subscriberChannelId ?: return
                val shareUrl = (buildShareLink() ?: return)
                    .apply {
                        dataModel.primaryLocales.value?.takeUnless { it.isEmpty() }
                            ?.joinToString(",") { LocaleCompat.toLanguageTag(it) }
                            ?.let { appendQueryParameter(PARAM_PRIMARY_LANGUAGE, it) }
                        dataModel.parallelLocales.value?.takeUnless { it.isEmpty() }
                            ?.joinToString(",") { LocaleCompat.toLanguageTag(it) }
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
            dataModel.tool.value?.let { tool -> downloadManager.downloadLatestPublishedTranslationAsync(tool, it) }
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
