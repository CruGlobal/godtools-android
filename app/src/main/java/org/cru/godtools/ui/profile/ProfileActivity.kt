package org.cru.godtools.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import me.thekey.android.TheKey
import me.thekey.android.livedata.getAttributesLiveData
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_GLOBAL_DASHBOARD
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.databinding.ProfileActivityBinding

fun Activity.startProfileActivity() = startActivity(
    Intent(this, ProfileActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
)

class ProfileActivity : BasePlatformActivity() {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfileActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setBindingData()
    }

    override fun onResume() {
        super.onResume()
        mEventBus.post(AnalyticsScreenEvent(SCREEN_GLOBAL_DASHBOARD))
    }
    // endregion Lifecycle

    // region Data Binding
    private lateinit var binding: ProfileActivityBinding

    private fun setBindingData() {
        val key = TheKey.getInstance(this)
        key.getAttributesLiveData().observe(this) {
            binding.accountName = "${it.firstName} ${it.lastName}"
        }
        binding.myProfileViewpager?.adapter = ProfilePageAdapter(this)
        val tabLayout = binding.myProfileTabLayout ?: return
        val viewPager = binding.myProfileViewpager ?: return
        TabLayoutMediator(tabLayout, viewPager) { tab, _ ->
            tab.text = getString(R.string.gt_gd_activity_text)
        }.attach()
    }
    // endregion Data Binding
}

private class ProfilePageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 1
    override fun createFragment(position: Int) = GlobalActivityFragment()
}
