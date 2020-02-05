package org.cru.godtools.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import me.thekey.android.TheKey
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_GLOBAL_DASHBOARD
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.databinding.ActivityMyProfileBinding
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.sync.syncGlobalAnalytics

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
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setBindingData()
    }

    override fun onResume() {
        super.onResume()
        mEventBus.post(AnalyticsScreenEvent(SCREEN_GLOBAL_DASHBOARD))
        binding?.myProfileTabLayout?.getTabAt(0)?.setText(R.string.gt_gd_activity_text)
        syncGlobalAnalytics(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
    // endregion lifeCycle Calls

    private fun setBindingData() {
        val key = TheKey.getInstance(this)
        binding?.accountName = "${key.cachedAttributes.firstName} ${key.cachedAttributes.lastName}"
        binding?.myProfileViewpager?.adapter = MyProfilePageAdapter(this)
        binding?.myProfileTabLayout?.let { tabLayout ->
            binding?.myProfileViewpager?.let { viewPager ->
                TabLayoutMediator(tabLayout, viewPager) { tab, _ ->
                    tab.text = getString(R.string.gt_gd_activity_text)
                    viewPager.setCurrentItem(tab.position, true)
                }.attach()
            }
        }
    }

    private class MyProfilePageAdapter(fm: FragmentActivity) :
        FragmentStateAdapter(fm) {
        override fun getItemCount() = 1
        override fun createFragment(position: Int) = GlobalDashboardFragment()
    }
}
