package org.cru.godtools.ui.languages

import android.app.Activity
import android.content.Intent
import androidx.annotation.MainThread
import androidx.fragment.app.commit
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_LANGUAGE_SETTINGS
import org.cru.godtools.base.Settings.Companion.FEATURE_LANGUAGE_SETTINGS
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.databinding.ActivityGenericFragmentWithNavDrawerBinding

fun Activity.startLanguageSettingsActivity() {
    Intent(this, LanguageSettingsActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        .putExtras(BaseActivity.buildExtras(this))
        .also { startActivity(it) }
}

class LanguageSettingsActivity : BasePlatformActivity<ActivityGenericFragmentWithNavDrawerBinding>() {
    // region Lifecycle
    override fun onContentChanged() {
        super.onContentChanged()
        loadPrimaryFragmentIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        settings.setFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS)
        eventBus.post(AnalyticsScreenEvent(SCREEN_LANGUAGE_SETTINGS))
    }
    // endregion Lifecycle

    override fun inflateBinding() = ActivityGenericFragmentWithNavDrawerBinding.inflate(layoutInflater)

    @MainThread
    private fun loadPrimaryFragmentIfNeeded() {
        with(supportFragmentManager) {
            if (primaryNavigationFragment != null) return

            commit {
                val fragment = LanguageSettingsFragment()
                replace(R.id.frame, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
    }
}
