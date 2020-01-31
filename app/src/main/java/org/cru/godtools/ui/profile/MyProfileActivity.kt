package org.cru.godtools.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import me.thekey.android.TheKey
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_GLOBAL_DASHBOARD
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.databinding.ActivityMyProfileBinding

fun Activity.startMyProfileActivity() {
    Intent(this, MyProfileActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
        .also { startActivity(it) }
}

class MyProfileActivity : BasePlatformActivity() {

    private var binding: ActivityMyProfileBinding? = null

    // region lifeCycle Calls
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater).also {
            setContentView(it.root.id)
        }
    }

    override fun onResume() {
        super.onResume()
        setBindingData()
        mEventBus.post(AnalyticsScreenEvent(SCREEN_GLOBAL_DASHBOARD))
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
    // endregion lifeCycle Calls

    private fun setBindingData() {
        val key = TheKey.getInstance(this)
        binding?.accountName = "${key.cachedAttributes.firstName} ${key.cachedAttributes.lastName}"
        binding?.myProfileTabLayout?.setupWithViewPager(binding?.myProfileViewpager)
        binding?.myProfileViewpager?.adapter = MyProfilePageAdapter(supportFragmentManager)
    }

    private class MyProfilePageAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int) = GlobalDashboardFragment()
        override fun getCount() = 1
    }
}
