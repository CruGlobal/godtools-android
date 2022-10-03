package org.cru.godtools.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.lifecycle.asLiveData
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.LaunchTrackingViewModel
import org.cru.godtools.base.EXTRA_PAGE
import org.cru.godtools.base.Settings.Companion.FEATURE_LANGUAGE_SETTINGS
import org.cru.godtools.base.Settings.Companion.FEATURE_PARALLEL_LANGUAGE
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.ActivityDashboardBinding
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.startTutorialActivity
import org.cru.godtools.ui.languages.paralleldialog.ParallelLanguageDialogFragment
import org.cru.godtools.ui.languages.startLanguageSettingsActivity
import org.cru.godtools.ui.tooldetails.startToolDetailsActivity
import org.cru.godtools.util.openToolActivity

private const val TAG_PARALLEL_LANGUAGE_DIALOG = "parallelLanguageDialog"

@AndroidEntryPoint
class DashboardActivity : BasePlatformActivity<ActivityDashboardBinding>() {
    private val viewModel: DashboardViewModel by viewModels()
    private val launchTrackingViewModel: LaunchTrackingViewModel by viewModels()

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
        binding.compose.setContent {
            GodToolsTheme {
                DashboardLayout(
                    onOpenTool = { t, tr1, tr2 -> openTool(t, tr1, tr2) },
                    onOpenToolDetails = { startToolDetailsActivity(it) }
                )
            }
        }
    }

    override fun onSetupActionBar() {
        super.onSetupActionBar()
        title = ""
    }

    override fun onNewIntent(newIntent: Intent) {
        super.onNewIntent(newIntent)
        processIntent(newIntent)
    }

    override fun onResume() {
        super.onResume()
        launchTrackingViewModel.trackLaunch()
    }
    // endregion Lifecycle

    // region Intent processing
    private fun processIntent(intent: Intent) {
        val page = (intent.getSerializableExtra(EXTRA_PAGE) as? Page)
        when {
            page != null -> viewModel.updateCurrentPage(page)
            intent.action == Intent.ACTION_VIEW -> {
                val data = intent.data
                when {
                    data?.isDashboardCustomUriSchemeDeepLink() == true ->
                        viewModel.updateCurrentPage(findPageByUriPathSegment(data.pathSegments.getOrNull(1)))
                    data?.isDashboardLessonsDeepLink() == true -> viewModel.updateCurrentPage(Page.LESSONS)
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
        viewModel.currentPage.map { it != Page.FAVORITE_TOOLS }.asLiveData()
    }

    override fun inflateBinding() = ActivityDashboardBinding.inflate(layoutInflater)

    private fun Menu.observeSelectedPageChanges() {
        findItem(R.id.action_switch_language)?.let { item ->
            viewModel.currentPage.asLiveData()
                .observe(this@DashboardActivity, item) { item.isVisible = it != Page.LESSONS }
        }
    }

    // region ToolsAdapterCallbacks
    @Inject
    internal lateinit var lazyManifestManager: Lazy<ManifestManager>
    private val manifestManager get() = lazyManifestManager.get()

    private fun openTool(tool: Tool?, primary: Translation?, parallel: Translation?) {
        val code = tool?.code ?: return
        val languages = listOfNotNull(primary?.languageCode, parallel?.languageCode)
        if (languages.isEmpty()) return

        languages.forEach { manifestManager.preloadLatestPublishedManifest(code, it) }
        openToolActivity(code, tool.type, *languages.toTypedArray())
    }
    // endregion ToolsAdapterCallbacks
    // endregion UI

    // region Feature Discovery
    private var featureDiscovery: TapTargetView? = null
    override fun isFeatureDiscoveryVisible() =
        super.isFeatureDiscoveryVisible() || isParallelLanguageDialogVisible() || featureDiscovery != null

    override fun canShowFeatureDiscovery(feature: String) = when (feature) {
        FEATURE_PARALLEL_LANGUAGE -> viewModel.currentPage.value == Page.HOME
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
