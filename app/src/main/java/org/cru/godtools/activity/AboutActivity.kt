package org.cru.godtools.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.fragment.app.transaction
import org.cru.godtools.R
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_ABOUT
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.util.LocaleUtils.getDeviceLocale
import org.cru.godtools.fragment.createAboutFragment

fun Activity.startAboutActivity() {
    Intent(this, AboutActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        .putExtras(BaseActivity.buildExtras(this))
        .also { startActivity(it) }
}

private const val TAG_MAIN_FRAGMENT = "mainFragment"

class AboutActivity : BasePlatformActivity() {
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
        mEventBus.post(AnalyticsScreenEvent(SCREEN_ABOUT, getDeviceLocale(this)))
    }

    // endregion Lifecycle Events

    @MainThread
    private fun loadInitialFragmentIfNeeded() {
        supportFragmentManager?.apply {
            if (findFragmentByTag(TAG_MAIN_FRAGMENT) == null) {
                transaction {
                    replace(R.id.frame, createAboutFragment(), TAG_MAIN_FRAGMENT)
                }
            }
        }
    }
}
