package org.keynote.godtools.android.activity

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.fragment.app.commit
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.transition.MaterialFadeThrough
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.LaunchTrackingViewModel
import org.cru.godtools.base.Settings.Companion.FEATURE_LANGUAGE_SETTINGS
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.databinding.ActivityDashboardBinding
import org.cru.godtools.model.Tool
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.activity.startTutorialActivity
import org.cru.godtools.ui.dashboard.DashboardSavedState
import org.cru.godtools.ui.dashboard.Page
import org.cru.godtools.ui.languages.startLanguageSettingsActivity
import org.cru.godtools.ui.tooldetails.startToolDetailsActivity
import org.cru.godtools.ui.tools.ToolsFragment
import org.cru.godtools.ui.tools.ToolsFragment.Companion.MODE_ADDED
import org.cru.godtools.ui.tools.ToolsFragment.Companion.MODE_ALL
import org.cru.godtools.util.openToolActivity

@AndroidEntryPoint
class MainActivity : BasePlatformActivity<ActivityDashboardBinding>(), ToolsFragment.Callbacks {
    private val savedState: DashboardSavedState by viewModels()
    private val launchTrackingViewModel: LaunchTrackingViewModel by viewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        triggerOnboardingIfNecessary()
    }

    override fun onCreateOptionsMenu(menu: Menu) = super.onCreateOptionsMenu(menu)
        .also { menuInflater.inflate(R.menu.activity_main, menu) }

    override fun onBindingChanged() {
        super.onBindingChanged()
        binding.setupBottomNavigation()
        showPage(savedState.selectedPage)
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

    private fun triggerOnboardingIfNecessary() {
        // TODO: remove this once we support onboarding in all languages
        // mark OnBoarding as discovered if this isn't a supported language
        if (!PageSet.ONBOARDING.supportsLocale(deviceLocale)) settings.setFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING)

        if (settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING)) return
        startTutorialActivity(PageSet.ONBOARDING)
    }

    // region UI
    override fun inflateBinding() = ActivityDashboardBinding.inflate(layoutInflater)
    override val toolbar get() = binding.appbar
    override val drawerLayout get() = binding.drawerLayout
    override val drawerMenu get() = binding.drawerMenu

    override val isShowNavigationDrawerIndicator get() = true

    private fun showPage(page: Page) {
        // short-circuit if the page is already displayed
        if (supportFragmentManager.primaryNavigationFragment != null && page == savedState.selectedPage) return

        val fragment = when (page) {
            Page.ALL_TOOLS -> ToolsFragment(MODE_ALL)
            Page.FAVORITE_TOOLS -> ToolsFragment(MODE_ADDED)
        }.apply {
            val transition = MaterialFadeThrough()
            enterTransition = transition
            exitTransition = transition
        }

        supportFragmentManager.commit {
            replace(R.id.frame, fragment)
            setPrimaryNavigationFragment(fragment)
        }
        savedState.selectedPage = page
    }

    // region ToolsFragment.Callbacks
    @Inject
    internal lateinit var lazyManifestManager: Lazy<ManifestManager>
    private val manifestManager get() = lazyManifestManager.get()

    override fun onToolSelect(code: String?, type: Tool.Type, vararg languages: Locale) {
        if (code == null || languages.isEmpty()) return
        languages.forEach { manifestManager.preloadLatestPublishedManifest(code, it) }
        openToolActivity(code, type, *languages)
    }

    override fun onToolInfo(code: String?) {
        code?.let { startToolDetailsActivity(code) }
    }

    override fun onNoToolsAvailableAction() = showPage(Page.ALL_TOOLS)
    // endregion ToolsFragment.Callbacks

    private fun ActivityDashboardBinding.setupBottomNavigation() {
        bottomNav.selectedItemId = savedState.selectedPage.id
        bottomNav.setOnNavigationItemSelectedListener {
            Page.findPage(it.itemId)?.let { showPage(it) }
            true
        }
    }
    // endregion UI

    // region Feature Discovery
    @JvmField
    protected var featureDiscovery: TapTargetView? = null
    override fun isFeatureDiscoveryVisible() = super.isFeatureDiscoveryVisible() || featureDiscovery != null

    override fun canShowFeatureDiscovery(feature: String) = when (feature) {
        FEATURE_LANGUAGE_SETTINGS -> !binding.drawerLayout.isDrawerOpen(GravityCompat.START)
        else -> super.canShowFeatureDiscovery(feature)
    }

    override fun showNextFeatureDiscovery() {
        if (!settings.isFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS) &&
            canShowFeatureDiscovery(FEATURE_LANGUAGE_SETTINGS)
        ) {
            dispatchDelayedFeatureDiscovery(FEATURE_LANGUAGE_SETTINGS, false, 15000)
            return
        }
        super.showNextFeatureDiscovery()
    }

    override fun onShowFeatureDiscovery(feature: String, force: Boolean) = when (feature) {
        FEATURE_LANGUAGE_SETTINGS -> {
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
                featureDiscoveryActive = feature
            } else {
                // TODO: we currently don't (can't?) distinguish between when the menu item doesn't exist and when
                //       the menu item just hasn't been drawn yet.

                // the toolbar action isn't available yet.
                // re-attempt this feature discovery on the next frame iteration.
                dispatchDelayedFeatureDiscovery(feature, force, 17)
            }
        }
        else -> super.onShowFeatureDiscovery(feature, force)
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
    // endregion Feature Discovery
}
