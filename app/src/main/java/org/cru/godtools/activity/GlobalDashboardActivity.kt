package org.cru.godtools.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import org.cru.godtools.R
import org.cru.godtools.fragment.GlobalDashboardFragment

private const val TAG_GLOBAL_DASHBOARD_FRAGMENT = "globalDashboardFragment"

fun Activity.startGlobalDashboardActvity() {
    startActivity(Intent(this, GlobalDashboardActivity::class.java))
}

class GlobalDashboardActivity : BasePlatformActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_generic_fragment_with_nav_drawer)
        loadInitialFragment()
    }

    private fun loadInitialFragment() {
        supportFragmentManager.commit {
            replace(R.id.frame, GlobalDashboardFragment(), TAG_GLOBAL_DASHBOARD_FRAGMENT)
        }
    }
}
