package org.cru.godtools.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import me.thekey.android.TheKey
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_GLOBAL_DASHBOARD
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.databinding.ActivityMyProfileBinding

fun Activity.startMyProfileActivity() {
    val intent = Intent(this, MyProfileActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
    startActivity(intent)
}

class MyProfileActivity : BasePlatformActivity() {

    // region lifeCycle Calls
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setBindingData(binding)
    }

    override fun onResume() {
        super.onResume()
        mEventBus.post(AnalyticsScreenEvent(SCREEN_GLOBAL_DASHBOARD))
    }
    // endregion lifeCycle Calls

    private fun setBindingData(binding: ActivityMyProfileBinding) {
        val key = TheKey.getInstance(this)
        binding.accountName = "${key.cachedAttributes.firstName} ${key.cachedAttributes.lastName}"
        binding.myProfileViewpager?.adapter = MyProfilePageAdapter(this)
        binding.myProfileTabLayout?.let { tabLayout ->
            binding.myProfileViewpager?.let { viewPager ->
                TabLayoutMediator(tabLayout, viewPager) { tab, _ ->
                    tab.text = getString(R.string.gt_gd_activity_text)
                    viewPager.setCurrentItem(tab.position, true)
                }.attach()
            }
        }
    }
}

private class MyProfilePageAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
    override fun getItemCount() = 1
    override fun createFragment(position: Int) = GlobalDashboardFragment()
}
