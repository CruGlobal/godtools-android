package org.cru.godtools.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.cru.godtools.R
import org.cru.godtools.fragment.GlobalDashboardFragment

private const val TAG_GLOBAL_DASHBOARD_FRAGMENT = "globalDashboardFragment"

fun Activity.startGlobalDashboardActvity() {
    Intent(this, GlobalDashboardActivity::class.java)
        .also { startActivity(it) }
}

class GlobalDashboardActivity : BasePlatformActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_generic_fragment_with_nav_drawer)
    }

    override fun onSetupActionBar() {
        super.onSetupActionBar()
        title = resources.getString(R.string.title_my_profile)
    }

    override fun onStart() {
        super.onStart()
    }

    private fun loadInitialFragment() {
        supportFragmentManager?.apply {
           beginTransaction().replace(R.id.frame, GlobalDashboardFragment(), TAG_GLOBAL_DASHBOARD_FRAGMENT)
        }
    }
}
