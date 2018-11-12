package org.cru.godtools.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.fragment.app.transaction
import org.cru.godtools.R
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_LANGUAGE_SETTINGS
import org.cru.godtools.base.Settings.FEATURE_LANGUAGE_SETTINGS
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.fragment.newLanguageSettingsFragment

private const val TAG_MAIN_FRAGMENT = "mainFragment"

fun Activity.startLanguageSettingsActivity() {
    Intent(this, LanguageSettingsActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        .putExtras(BaseActivity.buildExtras(this))
        .also { startActivity(it) }
}

class LanguageSettingsActivity : BasePlatformActivity() {
    // region Lifecycle Events

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic_fragment_with_nav_drawer)
    }

    override fun onStart() {
        super.onStart()
        loadInitialFragmentIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        prefs().setFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS)
        mEventBus.post(AnalyticsScreenEvent(SCREEN_LANGUAGE_SETTINGS))
    }

    // endregion Lifecycle Events

    @MainThread
    private fun loadInitialFragmentIfNeeded() {
        supportFragmentManager?.apply {
            if (findFragmentByTag(TAG_MAIN_FRAGMENT) == null) {
                transaction {
                    replace(R.id.frame, newLanguageSettingsFragment(), TAG_MAIN_FRAGMENT)
                }
            }
        }
    }
}
