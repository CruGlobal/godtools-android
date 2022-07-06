package org.cru.godtools.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.LaunchTrackingViewModel
import org.cru.godtools.base.EXTRA_PAGE
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_LANGUAGE_SETTINGS
import org.cru.godtools.base.Settings.Companion.FEATURE_PARALLEL_LANGUAGE
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.base.ui.util.getName
import org.cru.godtools.databinding.ActivityDashboardBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.activity.startTutorialActivity
import org.cru.godtools.ui.dashboard.home.HomeFragment
import org.cru.godtools.ui.dashboard.lessons.LessonsFragment
import org.cru.godtools.ui.dashboard.tools.ToolsFragment
import org.cru.godtools.ui.languages.paralleldialog.ParallelLanguageDialogFragment
import org.cru.godtools.ui.languages.startLanguageSettingsActivity
import org.cru.godtools.ui.tooldetails.startToolDetailsActivity
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks
import org.cru.godtools.ui.tools.analytics.model.ToolOpenTapAnalyticsActionEvent
import org.cru.godtools.util.openToolActivity

private const val TAG_PARALLEL_LANGUAGE_DIALOG = "parallelLanguageDialog"

@AndroidEntryPoint
class DashboardActivity :
    BasePlatformActivity<ActivityDashboardBinding>(R.layout.activity_dashboard),
    ToolsAdapterCallbacks,
    ToolsFragment.Callbacks,
    RemoveFavoriteConfirmationDialogFragment.Callbacks {
    private val dataModel: DashboardDataModel by viewModels()
    private val savedState: DashboardSavedState by viewModels()
    private val launchTrackingViewModel: LaunchTrackingViewModel by viewModels()

    @Inject
    internal lateinit var lazyDownloadManager: Lazy<GodToolsDownloadManager>
    private val downloadManager: GodToolsDownloadManager get() = lazyDownloadManager.get()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) intent?.let { processIntent(it) }
        triggerOnboardingIfNecessary()
    }

    override fun onCreateOptionsMenu(menu: Menu) = super.onCreateOptionsMenu(menu).also {
        menuInflater.inflate(R.menu.activity_main, menu)
        menu.observeSelectedPageChanges()
    }

    override fun onBindingChanged() {
        super.onBindingChanged()
        showPage(savedState.selectedPage)
        binding.selectedPage = savedState.selectedPageLiveData
        binding.setupBottomNavigation()
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        processIntent(newIntent)
    }

    override fun onResume() {
        super.onResume()
        launchTrackingViewModel.trackLaunch()
    }

    override fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) {
        super.onSyncData(helper, force)
        syncService.syncFollowups().sync()
        syncService.syncToolShares().sync()
    }
    // endregion Lifecycle

    // region Intent processing
    private fun processIntent(intent: Intent) {
        val page = (intent.getSerializableExtra(EXTRA_PAGE) as? Page)
        when {
            page != null -> showPage(page)
            intent.action == Intent.ACTION_VIEW -> {
                val data = intent.data
                when {
                    data?.isDashboardCustomUriSchemeDeepLink() == true ->
                        showPage(findPageByUriPathSegment(data.pathSegments.getOrNull(1)))
                    data?.isDashboardLessonsDeepLink() == true -> showPage(Page.LESSONS)
                }
            }
        }
    }
    // endregion Intent processing

    private fun triggerOnboardingIfNecessary() {
        if (settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING)) return
        startTutorialActivity(PageSet.ONBOARDING)
    }

    // region UI
    override val toolbar get() = binding.appbar
    override val drawerLayout get() = binding.drawerLayout
    override val drawerMenu get() = binding.drawerMenu

    override val isShowNavigationDrawerIndicator by lazy {
        savedState.selectedPageLiveData.map { it != Page.FAVORITE_TOOLS }
    }

    internal fun showPage(page: Page) {
        // short-circuit if the page is already displayed
        if (supportFragmentManager.primaryNavigationFragment != null && page == savedState.selectedPage) return

        val fragment = when (page) {
            Page.LESSONS -> LessonsFragment()
            Page.HOME -> HomeFragment()
            Page.ALL_TOOLS -> ToolsFragment()
            Page.FAVORITE_TOOLS -> null
        }

        if (fragment != null) {
            supportFragmentManager.commit {
                replace(R.id.frame, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
        savedState.selectedPage = page
    }

    private var selectedPageMenuObserver: Observer<Page>? = null
    private fun Menu.observeSelectedPageChanges() {
        selectedPageMenuObserver?.let { savedState.selectedPageLiveData.removeObserver(it) }
        selectedPageMenuObserver = Observer<Page> {
            findItem(R.id.action_switch_language)?.isVisible = it != Page.LESSONS
        }.also { savedState.selectedPageLiveData.observe(this@DashboardActivity, it) }
    }

    // region Remove Favorite Dialog
    private fun showRemoveFavoriteConfirmationDialog(tool: Tool?, translation: Translation?) {
        RemoveFavoriteConfirmationDialogFragment(tool?.code ?: return, translation.getName(tool, this).toString())
            .show(supportFragmentManager, null)
    }

    override fun removeFavorite(code: String) {
        downloadManager.unpinToolAsync(code)
    }
    // endregion Remove Favorite Dialog

    // region ToolsAdapterCallbacks
    @Inject
    internal lateinit var lazyManifestManager: Lazy<ManifestManager>
    private val manifestManager get() = lazyManifestManager.get()

    override fun openTool(tool: Tool?, primary: Translation?, parallel: Translation?) {
        val code = tool?.code ?: return
        val languages = listOfNotNull(primary?.languageCode, parallel?.languageCode)
        if (languages.isEmpty()) return

        languages.forEach { manifestManager.preloadLatestPublishedManifest(code, it) }
        eventBus.post(ToolOpenTapAnalyticsActionEvent)
        openToolActivity(code, tool.type, *languages.toTypedArray())
    }

    override fun showToolDetails(code: String?) {
        code?.let { startToolDetailsActivity(code) }
    }

    override fun pinTool(code: String?) {
        if (code == null) return
        downloadManager.pinToolAsync(code)
        settings.setFeatureDiscovered(Settings.FEATURE_TOOL_FAVORITE)
    }

    override fun unpinTool(tool: Tool?, translation: Translation?) {
        when (savedState.selectedPage) {
            Page.FAVORITE_TOOLS -> showRemoveFavoriteConfirmationDialog(tool, translation)
            else -> tool?.code?.let { removeFavorite(it) }
        }
    }
    // endregion ToolsAdapterCallbacks

    private fun ActivityDashboardBinding.setupBottomNavigation() {
        bottomNav.menu.findItem(R.id.dashboard_page_lessons)?.let { lessons ->
            dataModel.lessons.observe(this@DashboardActivity) { lessons.isVisible = !it.isNullOrEmpty() }
        }
        bottomNav.setOnItemSelectedListener {
            Page.findPage(it.itemId)?.let { showPage(it) }
            true
        }
    }
    // endregion UI

    // region Feature Discovery
    private var featureDiscovery: TapTargetView? = null
    override fun isFeatureDiscoveryVisible() =
        super.isFeatureDiscoveryVisible() || isParallelLanguageDialogVisible() || featureDiscovery != null

    override fun canShowFeatureDiscovery(feature: String) = when (feature) {
        FEATURE_PARALLEL_LANGUAGE -> savedState.selectedPage == Page.FAVORITE_TOOLS
        FEATURE_LANGUAGE_SETTINGS -> !binding.drawerLayout.isDrawerOpen(GravityCompat.START)
        else -> super.canShowFeatureDiscovery(feature)
    }

    override fun showNextFeatureDiscovery() = when {
        !settings.isFeatureDiscovered(FEATURE_PARALLEL_LANGUAGE) &&
            canShowFeatureDiscovery(FEATURE_PARALLEL_LANGUAGE) ->
            showFeatureDiscovery(FEATURE_PARALLEL_LANGUAGE, false)
        !settings.isFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS) &&
            canShowFeatureDiscovery(FEATURE_LANGUAGE_SETTINGS) ->
            dispatchDelayedFeatureDiscovery(FEATURE_LANGUAGE_SETTINGS, false, 15000)
        else -> super.showNextFeatureDiscovery()
    }

    override fun onShowFeatureDiscovery(feature: String, force: Boolean) = when (feature) {
        FEATURE_PARALLEL_LANGUAGE -> showParallelLanguageDialog()
        FEATURE_LANGUAGE_SETTINGS -> showLanguageSettingsFeatureDiscovery(force)
        else -> super.onShowFeatureDiscovery(feature, force)
    }

    // region Parallel Language
    private fun showParallelLanguageDialog() {
        ParallelLanguageDialogFragment().show(supportFragmentManager, TAG_PARALLEL_LANGUAGE_DIALOG)
        settings.setFeatureDiscovered(FEATURE_PARALLEL_LANGUAGE)
    }

    private fun isParallelLanguageDialogVisible() =
        supportFragmentManager.findFragmentByTag(TAG_PARALLEL_LANGUAGE_DIALOG) != null
    // endregion Parallel Language

    // region Language Settings
    private fun showLanguageSettingsFeatureDiscovery(force: Boolean) {
        if (toolbar.findViewById<View>(R.id.action_switch_language) != null) {
            // purge any pending feature discovery triggers since we are showing feature discovery now
            purgeQueuedFeatureDiscovery(FEATURE_LANGUAGE_SETTINGS)

            // show language settings feature discovery
            val target = TapTarget.forToolbarMenuItem(
                toolbar,
                R.id.action_switch_language,
                getString(R.string.feature_discovery_title_language_settings),
                getString(R.string.feature_discovery_desc_language_settings)
            )
            featureDiscovery = TapTargetView.showFor(this, target, LanguageSettingsFeatureDiscoveryListener())
            featureDiscoveryActive = FEATURE_LANGUAGE_SETTINGS
        } else {
            // TODO: we currently don't (can't?) distinguish between when the menu item doesn't exist and when
            //       the menu item just hasn't been drawn yet.

            // the toolbar action isn't available yet.
            // re-attempt this feature discovery on the next frame iteration.
            dispatchDelayedFeatureDiscovery(FEATURE_LANGUAGE_SETTINGS, force, 17)
        }
    }

    private inner class LanguageSettingsFeatureDiscoveryListener : TapTargetView.Listener() {
        override fun onTargetClick(view: TapTargetView) {
            super.onTargetClick(view)
            startLanguageSettingsActivity()
        }

        override fun onOuterCircleClick(view: TapTargetView) {
            onTargetCancel(view)
        }

        override fun onTargetDismissed(view: TapTargetView, userInitiated: Boolean) {
            super.onTargetDismissed(view, userInitiated)
            if (userInitiated) {
                settings.setFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS)
                featureDiscoveryActive = null
                showNextFeatureDiscovery()
            }
            if (view === featureDiscovery) featureDiscovery = null
        }
    }
    // endregion Language Settings
    // endregion Feature Discovery

    override fun onSupportNavigateUp() = when {
        onBackPressedDispatcher.hasEnabledCallbacks() -> {
            onBackPressedDispatcher.onBackPressed()
            true
        }
        else -> super.onSupportNavigateUp()
    }
}
