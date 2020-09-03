package org.keynote.godtools.android.activity

import android.view.View
import androidx.core.view.GravityCompat
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.base.Settings.Companion.FEATURE_LANGUAGE_SETTINGS
import org.cru.godtools.databinding.ActivityDashboardBinding
import org.cru.godtools.ui.languages.startLanguageSettingsActivity

abstract class KotlinMainActivity : BasePlatformActivity<ActivityDashboardBinding>() {
    // region Feature Discovery
    @JvmField
    protected var featureDiscovery: TapTargetView? = null
    override fun isFeatureDiscoveryVisible() = super.isFeatureDiscoveryVisible() || featureDiscovery != null

    override fun canShowFeatureDiscovery(feature: String) = when (feature) {
        FEATURE_LANGUAGE_SETTINGS -> toolbar != null && !binding.drawerLayout.isDrawerOpen(GravityCompat.START)
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
            val toolbar = toolbar
            if (toolbar?.findViewById<View>(R.id.action_switch_language) != null) {
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
