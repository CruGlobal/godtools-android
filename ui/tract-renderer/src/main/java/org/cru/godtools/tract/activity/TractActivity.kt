package org.cru.godtools.tract.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.observe
import com.google.android.instantapps.InstantApps
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.delay
import org.ccci.gto.android.common.androidx.fragment.app.BaseDialogFragment
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.notNull
import org.ccci.gto.android.common.androidx.lifecycle.observeOnce
import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.ccci.gto.android.common.compat.view.ViewCompat
import org.ccci.gto.android.common.util.LocaleUtils
import org.ccci.gto.android.common.util.findListener
import org.ccci.gto.android.common.util.os.getLocaleArray
import org.ccci.gto.android.common.util.os.putLocaleArray
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.base.Constants.EXTRA_TOOL
import org.cru.godtools.base.Constants.URI_SHARE_BASE
import org.cru.godtools.base.model.Event
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.cru.godtools.base.tool.model.view.ManifestViewUtils
import org.cru.godtools.tract.Constants.PARAM_LIVE_SHARE_STREAM
import org.cru.godtools.tract.Constants.PARAM_PARALLEL_LANGUAGE
import org.cru.godtools.tract.Constants.PARAM_PRIMARY_LANGUAGE
import org.cru.godtools.tract.Constants.PARAM_USE_DEVICE_LANGUAGE
import org.cru.godtools.tract.R
import org.cru.godtools.tract.adapter.ManifestPagerAdapter
import org.cru.godtools.tract.analytics.model.ToggleLanguageAnalyticsActionEvent
import org.cru.godtools.tract.analytics.model.TractPageAnalyticsScreenEvent
import org.cru.godtools.tract.databinding.TractActivityBinding
import org.cru.godtools.tract.liveshare.TractPublisherController
import org.cru.godtools.tract.liveshare.TractSubscriberController
import org.cru.godtools.tract.service.FollowupService
import org.cru.godtools.tract.util.ViewUtils
import org.cru.godtools.xml.model.Card
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Modal
import org.cru.godtools.xml.model.Page
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Locale
import javax.inject.Inject
import org.cru.godtools.tract.liveshare.Event as LiveShareEvent

private const val EXTRA_LANGUAGES = "org.cru.godtools.tract.activity.TractActivity.LANGUAGES"
private const val EXTRA_INITIAL_PAGE = "org.cru.godtools.tract.activity.TractActivity.INITIAL_PAGE"

fun Activity.startTractActivity(toolCode: String, vararg languages: Locale?) =
    startActivity(createTractActivityIntent(toolCode, *languages))

fun Context.createTractActivityIntent(toolCode: String, vararg languages: Locale?) =
    Intent(this, TractActivity::class.java)
        .putExtras(Bundle().populateTractActivityExtras(toolCode, *languages))

private fun Bundle.populateTractActivityExtras(toolCode: String, vararg languages: Locale?) = apply {
    putString(EXTRA_TOOL, toolCode)
    // XXX: we use singleString mode to support using this intent for legacy shortcuts
    putLocaleArray(EXTRA_LANGUAGES, languages.filterNotNull().toTypedArray(), true)
}

class TractActivity : BaseToolActivity<TractActivityBinding>(true, R.layout.tract_activity),
    TabLayout.OnTabSelectedListener, ManifestPagerAdapter.Callbacks {
    // Inject the FollowupService to ensure it is running to capture any followup forms
    @Inject
    internal lateinit var followupService: FollowupService

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
        startLiveShareSubscriberIfNecessary()
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
        if (InstantApps.isInstantApp(this)) toolbar?.setNavigationIcon(R.drawable.ic_close)
    }

    override fun onCreateOptionsMenu(menu: Menu) = super.onCreateOptionsMenu(menu).also {
        menuInflater.inflate(R.menu.activity_tract, menu)

        // make the install menu item visible if this is an Instant App
        menu.findItem(R.id.action_install)?.isVisible = InstantApps.isInstantApp(this)
    }

    override fun onStart() {
        super.onStart()
        eventBus.register(this)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when {
        item.itemId == R.id.action_install -> {
            InstantApps.showInstallPrompt(this, -1, "instantapp")
            true
        }
        item.itemId == R.id.action_live_share_publish -> {
            publisherController.stateMachine.transition(LiveShareEvent.Start)
            shareLiveShareLink()
            true
        }
        // handle close button if this is an instant app
        item.itemId == android.R.id.home && InstantApps.isInstantApp(this) -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @CallSuper
    override fun onUpdateActiveManifest() {
        super.onUpdateActiveManifest()
        showNextFeatureDiscovery()
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onContentEvent(event: Event) {
        checkForPageEvent(event)
    }

    override fun onUpdateActiveCard(page: Page, card: Card?) {
        trackTractPage(page, card)
        sendLiveShareNavigationEvent(page, card)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_INITIAL_PAGE, initialPage)
    }

    override fun onStop() {
        eventBus.unregister(this)
        super.onStop()
    }
    // endregion Lifecycle

    // region Intent Processing
    private fun processIntent(intent: Intent?, savedInstanceState: Bundle?) {
        val data = intent?.data
        val extras = intent?.extras
        if (intent?.action == Intent.ACTION_VIEW && data != null && isDeepLinkValid(data)) {
            dataModel.tool.value = data.extractToolFromDeepLink()
            val (primary, parallel) = data.extractLanguagesFromDeepLink()
            dataModel.primaryLocales.value = primary
            dataModel.parallelLocales.value = parallel
            if (savedInstanceState == null) data.extractPageFromDeepLink()?.let { initialPage = it }
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
    fun isDeepLinkValid(data: Uri?) = data != null &&
        ("http".equals(data.scheme, true) || "https".equals(data.scheme, true)) &&
        (getString(R.string.tract_deeplink_host_1).equals(data.host, true) ||
            getString(R.string.tract_deeplink_host_2).equals(data.host, true)) &&
        data.pathSegments.size >= 2

    @VisibleForTesting
    fun Uri.extractToolFromDeepLink() = pathSegments.getOrNull(1)

    @OptIn(ExperimentalStdlibApi::class)
    private fun Uri.extractLanguagesFromDeepLink(): Pair<List<Locale>, List<Locale>> {
        val primary = LocaleUtils.getFallbacks(*buildList<Locale> {
            if (!getQueryParameter(PARAM_USE_DEVICE_LANGUAGE).isNullOrEmpty()) add(Locale.getDefault())
            addAll(extractLanguagesFromDeepLinkParam(PARAM_PRIMARY_LANGUAGE))
            add(LocaleCompat.forLanguageTag(pathSegments[0]))
        }.toTypedArray())

        val parallel =
            LocaleUtils.getFallbacks(*extractLanguagesFromDeepLinkParam(PARAM_PARALLEL_LANGUAGE).toTypedArray())
        return Pair(primary.toList(), parallel.toList())
    }

    private fun Uri.extractLanguagesFromDeepLinkParam(param: String) = getQueryParameters(param)
        .flatMap { it.split(",") }
        .map { it.trim() }.filterNot { it.isEmpty() }
        .map { LocaleCompat.forLanguageTag(it) }

    @VisibleForTesting
    fun Uri.extractPageFromDeepLink() = pathSegments.getOrNull(2)?.toIntOrNull()
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
        dataModel.activeManifest.observe(this) {
            window.decorView.setBackgroundColor(Manifest.getBackgroundColor(it))
            ManifestViewUtils.bindBackgroundImage(it, binding.backgroundImage)
        }
    }

    // region Language Toggle
    private fun setupLanguageToggle() {
        ViewCompat.setClipToOutline(binding.languageToggle, true)
        binding.languageToggle.addOnTabSelectedListener(this)
        dataModel.activeManifest.observe(this) { manifest ->
            // determine colors for the language toggle
            val controlColor = Manifest.getNavBarControlColor(manifest)
            var selectedColor = Manifest.getNavBarColor(manifest)
            if (Color.alpha(selectedColor) < 255) {
                // XXX: the expected behavior is to support transparent text. But we currently don't support
                // XXX: transparent text, so pick white or black based on the control color
                val hsv = FloatArray(3)
                Color.colorToHSV(controlColor, hsv)
                selectedColor = if (hsv[2] > 0.6) Color.BLACK else Color.WHITE
            }

            // update colors for tab text, and background
            binding.languageToggle.setTabTextColors(controlColor, selectedColor)
            ViewUtils.setBackgroundTint(binding.languageToggle, controlColor)
        }

        val controller = LanguageToggleController(binding.languageToggle)
        dataModel.activeLocale.observe(this) { controller.activeLocale = it }
        dataModel.activeManifest.observe(this) { controller.activeManifest = it }
        dataModel.visibleLocales.observe(this) { controller.locales = it }
        dataModel.languages.observe(this) { controller.languages = it }
    }

    // region TabLayout.OnTabSelectedListener
    override fun onTabSelected(tab: TabLayout.Tab) {
        val locale = tab.tag as? Locale ?: return
        dataModel.setActiveLocale(locale)
        eventBus.post(ToggleLanguageAnalyticsActionEvent(dataModel.tool.value, locale))
    }

    override fun onTabReselected(tab: TabLayout.Tab?) = Unit
    override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
    // endregion TabLayout.OnTabSelectedListener
    // endregion Language Toggle

    // region Tool Pager
    private val pager get() = binding.pages
    private val pagerAdapter by lazy {
        ManifestPagerAdapter().also {
            it.setCallbacks(this)
            lifecycle.addObserver(it)
            dataModel.activeManifest.observe(this, it)
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
            pagerAdapter.setManifest(it)
            pager.setCurrentItem(initialPage, false)
            initialPage = -1
        }
    }

    private fun checkForPageEvent(event: Event) {
        activeManifest?.pages?.firstOrNull { it.listeners.contains(event.id) }?.let { goToPage(it.position) }
    }

    // region ManifestPagerAdapter.Callbacks
    override fun goToPage(position: Int) {
        pager.currentItem = position
    }

    override fun showModal(modal: Modal) = startModalActivity(modal)
    // endregion ManifestPagerAdapter.Callbacks
    // endregion Tool Pager
    // endregion UI

    override fun cacheTools() {
        dataModel.tool.value?.let { tool ->
            dataModel.locales.value?.forEach { downloadManager.cacheTranslation(tool, it) }
        }
    }

    private fun trackTractPage(page: Page, card: Card?) = eventBus.post(
        TractPageAnalyticsScreenEvent(page.manifest.code, page.manifest.locale, page.position, card?.position)
    )

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

    // region Share Link Logic
    override fun hasShareLinkUri() = activeManifest != null
    override val shareLinkUri
        get() = activeManifest?.let {
            URI_SHARE_BASE.buildUpon()
                .appendEncodedPath(LocaleCompat.toLanguageTag(it.locale).toLowerCase(Locale.ENGLISH))
                .appendPath(it.code)
                .apply { if (pager.currentItem > 0) appendPath(pager.currentItem.toString()) }
                .apply {
                    publisherController.publisherInfo.value?.subscriberChannelId
                        ?.let { appendQueryParameter(PARAM_LIVE_SHARE_STREAM, it) }
                }
                .appendQueryParameter("icid", "gtshare")
                .build().toString()
        }
    // endregion Share Link Logic

    // region Live Share Logic
    private val publisherController: TractPublisherController by viewModels()
    private val subscriberController: TractSubscriberController by viewModels()

    fun shareLiveShareLink() {
        if (publisherController.publisherInfo.value == null) {
            LiveShareDialogFragment().show(supportFragmentManager, null)
        } else {
            shareCurrentTool()
        }
    }

    private fun sendLiveShareNavigationEvent(page: Page, card: Card?) {
        publisherController.sendNavigationEvent(
            NavigationEvent(page.manifest.code, page.manifest.locale, page.position, card?.position)
        )
    }

    private fun startLiveShareSubscriberIfNecessary() {
        val streamId = intent?.data?.getQueryParameter(PARAM_LIVE_SHARE_STREAM) ?: return

        subscriberController.channelId = streamId
        subscriberController.receivedEvent.notNull().distinctUntilChanged()
            .observe(this) { navigateToLiveShareEvent(it) }
    }

    private fun navigateToLiveShareEvent(event: NavigationEvent?) {
        if (event == null) return
        event.page?.let { goToPage(it) }
        eventBus.post(event)
    }
    // endregion Live Share Logic
}

class LiveShareDialogFragment : BaseDialogFragment() {
    private val publisherController: TractPublisherController by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        liveData {
            delay(2_000)
            emitSource(publisherController.publisherInfo)
        }.observe(this) {
            findListener<TractActivity>()?.shareLiveShareLink()
            dismissAllowingStateLoss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Starting Screen Sharing...")
            .setView(R.layout.tract_live_share_dialog)
            .create()
    }
}
