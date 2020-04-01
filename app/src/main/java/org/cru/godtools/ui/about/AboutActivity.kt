package org.cru.godtools.ui.about

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.fragment.app.commit
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_ABOUT
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.util.deviceLocale

fun Activity.startAboutActivity() {
    Intent(this, AboutActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        .putExtras(BaseActivity.buildExtras(this))
        .also { startActivity(it) }
}

class AboutActivity : BasePlatformActivity() {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic_fragment_with_nav_drawer)
        loadPrimaryFragmentIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        eventBus.post(AnalyticsScreenEvent(SCREEN_ABOUT, deviceLocale))
    }
    // endregion Lifecycle

    @MainThread
    private fun loadPrimaryFragmentIfNeeded() {
        with(supportFragmentManager) {
            if (primaryNavigationFragment != null) return

            commit {
                val fragment = AboutFragment()
                replace(R.id.frame, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
    }
}
