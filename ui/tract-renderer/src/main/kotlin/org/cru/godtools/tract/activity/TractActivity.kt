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
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.google.android.instantapps.InstantApps
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.androidx.fragment.app.showAllowingStateLoss
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.notNull
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.ccci.gto.android.common.androidx.lifecycle.observeOnce
import org.ccci.gto.android.common.util.includeFallbacks
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.base.EXTRA_PAGE
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.SCHEME_GODTOOLS
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_LIVE_SHARE
import org.cru.godtools.base.URI_SHARE_BASE
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivity
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.shareable.model.ShareableImageShareItem
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.backgroundColor
import org.cru.godtools.shared.tool.parser.model.tips.Tip
import org.cru.godtools.shared.tool.parser.model.tract.Modal
import org.cru.godtools.shared.tool.parser.model.tract.TractPage
import org.cru.godtools.shared.tool.parser.model.tract.TractPage.Card
import org.cru.godtools.tool.tips.ui.TipBottomSheetDialogFragment
import org.cru.godtools.tool.tract.BuildConfig.HOST_GODTOOLS_CUSTOM_URI
import org.cru.godtools.tool.tract.R
import org.cru.godtools.tool.tract.databinding.TractActivityBinding
import org.cru.godtools.tract.PARAM_LIVE_SHARE_STREAM
import org.cru.godtools.tract.PARAM_PARALLEL_LANGUAGE
import org.cru.godtools.tract.PARAM_PRIMARY_LANGUAGE
import org.cru.godtools.tract.PARAM_USE_DEVICE_LANGUAGE
import org.cru.godtools.tract.adapter.ManifestPagerAdapter
import org.cru.godtools.tract.analytics.model.ShareScreenEngagedActionEvent
import org.cru.godtools.tract.analytics.model.ShareScreenOpenedActionEvent
import org.cru.godtools.tract.analytics.model.TractPageAnalyticsScreenEvent
import org.cru.godtools.tract.liveshare.State
import org.cru.godtools.tract.liveshare.TractPublisherController
import org.cru.godtools.tract.liveshare.TractSubscriberController
import org.cru.godtools.tract.ui.liveshare.LiveShareExitDialogFragment
import org.cru.godtools.tract.ui.liveshare.LiveShareStartingDialogFragment
import org.cru.godtools.tract.ui.settings.SettingsBottomSheetDialogFragment
import org.cru.godtools.tract.util.isTractDeepLink
import org.cru.godtools.tract.util.loadAnimation
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.TutorialActivityResultContract

private const val EXTRA_INITIAL_PAGE = "org.cru.godtools.tract.activity.TractActivity.INITIAL_PAGE"

@AndroidEntryPoint
class TractActivity :
    MultiLanguageToolActivity<TractActivityBinding>(R.layout.tract_activity, Manifest.Type.TRACT),
    ManifestPagerAdapter.Callbacks,
    TipBottomSheetDialogFragment.Callbacks {
    private val savedState: TractActivitySavedState by viewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return

        // restore any saved state
        savedInstanceState?.run {
            initialPage = getInt(EXTRA_INITIAL_PAGE, initialPage)
        }

        // track this tool open
        if (savedInstanceState == null) dataModel.toolCode.value?.let { trackToolOpen(it, Manifest.Type.TRACT) }

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
        if (InstantApps.isInstantApp(this)) toolbar.setNavigationIcon(org.cru.godtools.ui.R.drawable.ic_close)
    }

    override fun onCreateOptionsMenu(menu: Menu) = super.onCreateOptionsMenu(menu).also {
        menuInflater.inflate(R.menu.activity_tract, menu)
        menu.removeItem(R.id.action_share)
        menu.removeItem(R.id.action_tips)
        menuInflater.inflate(R.menu.activity_tract_live_share, menu)
        menu.setupLiveShareMenuItem()

        // Adjust visibility of menu items
        menu.findItem(R.id.action_install)?.isVisible = InstantApps.isInstantApp(this)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when {
        item.itemId == R.id.action_install -> {
            InstantApps.showInstallPrompt(this, intent, -1, "instantapp")
            true
        }
        item.itemId == R.id.action_settings -> {
            SettingsBottomSheetDialogFragment().show(supportFragmentManager, null)
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
    override fun processIntent(intent: Intent, savedInstanceState: Bundle?) {
        super.processIntent(intent, savedInstanceState)
        if (savedInstanceState == null) initialPage = intent.getIntExtra(EXTRA_PAGE, initialPage)

        // deep link parsing
        if (
            savedInstanceState == null ||
            (dataModel.primaryLocales.value.isNullOrEmpty() && dataModel.parallelLocales.value.isNullOrEmpty())
        ) {
            if (intent.action != Intent.ACTION_VIEW) return
            val data = intent.data?.normalizeScheme() ?: return
            val path = data.pathSegments ?: return

            when {
                data.isTractDeepLink() -> {
                    dataModel.toolCode.value = path[1]
                    val (primary, parallel) = data.deepLinkLanguages
                    dataModel.primaryLocales.value = primary
                    dataModel.parallelLocales.value = parallel
                    if (savedInstanceState == null) {
                        dataModel.activeLocale.value = data.deepLinkSelectedLanguage
                        data.deepLinkPage?.let { initialPage = it }
                    }
                }
                data.isGodToolsDeepLink() -> {
                    dataModel.toolCode.value = path[3]
                    dataModel.primaryLocales.value =
                        sequenceOf(Locale.forLanguageTag(path[4])).includeFallbacks().toList()
                    path.getOrNull(5)?.toIntOrNull()?.let { initialPage = it }
                }
                data.isCustomUriDeepLink() -> {
                    dataModel.toolCode.value = path[2]
                    dataModel.primaryLocales.value =
                        sequenceOf(Locale.forLanguageTag(path[3])).includeFallbacks().toList()
                    path.getOrNull(4)?.toIntOrNull()?.let { initialPage = it }
                }
            }
        }
    }

    private fun Uri.isGodToolsDeepLink() = (scheme == "http" || scheme == "https") &&
        HOST_GODTOOLSAPP_COM.equals(host, true) && pathSegments.orEmpty().size >= 5 &&
        path?.startsWith("/deeplink/tool/tract/") == true

    private fun Uri.isCustomUriDeepLink() = scheme == SCHEME_GODTOOLS &&
        HOST_GODTOOLS_CUSTOM_URI.equals(host, true) && pathSegments.orEmpty().size >= 4 &&
        pathSegments?.getOrNull(0) == "tool" && pathSegments?.getOrNull(1) == "tract"

    @VisibleForTesting
    internal val Uri.deepLinkSelectedLanguage get() = Locale.forLanguageTag(pathSegments[0])
    @VisibleForTesting
    internal val Uri.deepLinkPage get() = pathSegments.getOrNull(2)?.toIntOrNull()

    @VisibleForTesting
    internal val Uri.deepLinkLanguages: Pair<List<Locale>, List<Locale>> get() {
        val primary = LinkedHashSet<Locale>()
        val parallel = LinkedHashSet<Locale>()
        val selected = deepLinkSelectedLanguage

        if (getQueryParameter(PARAM_USE_DEVICE_LANGUAGE)?.isNotEmpty() == true) {
            primary += sequenceOf(Locale.getDefault()).includeFallbacks()
        }
        primary += extractLanguagesFromDeepLinkParam(PARAM_PRIMARY_LANGUAGE).includeFallbacks()
        parallel += extractLanguagesFromDeepLinkParam(PARAM_PARALLEL_LANGUAGE).includeFallbacks()

        if (selected !in primary && selected !in parallel) primary += sequenceOf(selected).includeFallbacks()

        return Pair(primary.toList(), parallel.toList())
    }

    private fun Uri.extractLanguagesFromDeepLinkParam(param: String) = getQueryParameters(param)
        .asSequence()
        .flatMap { it.split(",") }
        .map { it.trim() }
        .filterNot { it.isEmpty() }
        .map { Locale.forLanguageTag(it) }
    // endregion Intent Processing

    // region UI
    override val toolbar get() = binding.appbar
    override val languageToggle get() = binding.languageToggle

    private fun setupBinding() {
        binding.activeLocale = dataModel.activeLocale
        binding.visibleLocales = dataModel.visibleLocales
    }

    private fun setupBackground() {
        dataModel.activeManifest.observe(this) { window.decorView.setBackgroundColor(it.backgroundColor) }
    }

    // region Tool Pager
    @Inject
    internal lateinit var pagerAdapterFactory: ManifestPagerAdapter.Factory
    private val pager get() = binding.pages
    private val pagerAdapter by lazy {
        pagerAdapterFactory.create(this, dataModel.enableTips, toolState.toolState).also { adapter ->
            adapter.callbacks = this
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
    @VisibleForTesting
    internal var initialPage = 0

    private fun setupPager() {
        pager.adapter = pagerAdapter
        dataModel.visibleLocales.observe(this) {
            pager.layoutDirection = TextUtils.getLayoutDirectionFromLocale(it.firstOrNull())
        }

        if (initialPage >= 0) {
            dataModel.activeManifest.notNull().observeOnce(this) {
                if (initialPage < 0) return@observeOnce

                // HACK: set the manifest in the pager adapter to ensure setCurrentItem works.
                //       This is normally handled by the pager adapter observer.
                pagerAdapter.manifest = it
                pager.setCurrentItem(initialPage, false)
                initialPage = -1
            }
        }
    }

    private fun checkForPageEvent(event: Event) {
        activeManifest?.pages?.firstOrNull { it.listeners.contains(event.id) }?.let { goToPage(it.position) }
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
        // HACK: make this dependent on shareLinkUriLiveData so that there is a subscriber to actually resolve the uri
        //       before the user clicks the share action
        shareLinkUriLiveData.map { false }
    }

    override val shareLinkUriLiveData by lazy {
        viewModel.manifest.map { it?.buildShareLink()?.build()?.toString() }.asLiveData()
    }
    private fun Manifest.buildShareLink(page: Int = pager.currentItem): Uri.Builder? {
        val tool = code ?: return null
        val locale = locale ?: return null
        return URI_SHARE_BASE.buildUpon()
            .appendEncodedPath(locale.toLanguageTag().lowercase(Locale.ENGLISH))
            .appendPath(tool)
            .apply { if (page > 0) appendPath(page.toString()) }
            .appendQueryParameter("icid", "gtshare")
    }

    override fun getShareableShareItems() = emptyList<ShareableImageShareItem>()
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
    private fun Menu.setupLiveShareMenuItem() {
        findItem(R.id.action_live_share_active)?.let { item ->
            item.loadAnimation(this@TractActivity, R.raw.anim_tract_live_share)

            liveShareState.observe(this@TractActivity, item) { (publisherState, subscriberState) ->
                isVisible = publisherState == State.On || subscriberState == State.On
            }
        }
    }

    internal fun shareLiveShareLink() {
        publisherController.started = true
        when {
            !savedState.liveShareTutorialShown &&
                settings.getFeatureDiscoveredCount("$FEATURE_TUTORIAL_LIVE_SHARE${dataModel.toolCode.value}") < 3 ->
                liveShareTutorialLauncher.launch(PageSet.LIVE_SHARE)
            publisherController.publisherInfo.value == null ->
                LiveShareStartingDialogFragment().showAllowingStateLoss(supportFragmentManager, null)
            else -> {
                val subscriberId = publisherController.publisherInfo.value?.subscriberChannelId ?: return
                val shareUrl = (activeManifest?.buildShareLink() ?: return)
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
                eventBus.post(ShareScreenEngagedActionEvent(dataModel.toolCode.value))
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
            // The requested locale is not an available locale, so add it as a parallelLocale
            if (it !in dataModel.locales.value) {
                dataModel.parallelLocales.value = dataModel.parallelLocales.value.orEmpty() + it
            }
            dataModel.activeLocale.value = it
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
