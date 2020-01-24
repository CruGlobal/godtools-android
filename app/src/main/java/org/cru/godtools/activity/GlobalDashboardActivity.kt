package org.cru.godtools.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import org.cru.godtools.R
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_GLOBAL_DASHBOARD
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.fragment.GlobalDashboardFragment

fun Activity.startGlobalDashboardActivity() {
    Intent(this, GlobalDashboardActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
        .also { startActivity(it) }
}

class GlobalDashboardActivity : BasePlatformActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic_fragment)
    }

    override fun onStart() {
        super.onStart()
        loadGlobalDashboardFragment()
    }

    override fun onResume() {
        super.onResume()
        mEventBus.post(AnalyticsScreenEvent(SCREEN_GLOBAL_DASHBOARD))
    }

    fun loadGlobalDashboardFragment() {
        with(supportFragmentManager) {
            commit {
                val fragment = GlobalDashboardFragment()
                replace(R.id.frame, fragment)
            }
        }
    }
}
