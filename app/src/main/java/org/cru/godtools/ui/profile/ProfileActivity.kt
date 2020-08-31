package org.cru.godtools.ui.profile

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import me.thekey.android.livedata.getAttributesLiveData
import org.ccci.gto.android.common.androidx.viewpager2.widget.setHeightWrapContent
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

@AndroidEntryPoint
class ProfileActivity : BasePlatformActivity<ProfileActivityBinding>(R.layout.profile_activity) {
    // region Lifecycle
    override fun onContentChanged() {
        super.onContentChanged()
        setupDataBinding()
        setupPages()
    }

    override fun onResume() {
        super.onResume()
        eventBus.post(AnalyticsScreenEvent(SCREEN_GLOBAL_DASHBOARD))
    }
    // endregion Lifecycle

    // region Data Binding
    private fun setupDataBinding() {
        binding.keyAttributes = theKey.getAttributesLiveData()
    }
    // endregion Data Binding

    override val toolbar get() = binding.appbar
    override val swipeRefreshLayout get() = binding.refresh

    // region Pages
    private fun setupPages() {
        binding.pages.setHeightWrapContent()
        binding.pages.adapter = ProfilePageAdapter(this)
        TabLayoutMediator(binding.tabs, binding.pages) { tab: TabLayout.Tab, i: Int ->
            when (i) {
                0 -> tab.setText(R.string.profile_tab_activity)
            }
        }.attach()
    }
    // endregion Pages
}

private class ProfilePageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount() = 1
    override fun createFragment(position: Int) = when (position) {
        0 -> GlobalActivityFragment()
        else -> throw IllegalArgumentException("Invalid position")
    }
}
