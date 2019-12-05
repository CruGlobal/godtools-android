package org.cru.godtools.tutorial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewpager.widget.PagerAdapter
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.databinding.BakedInOnboardingFinalBinding
import org.cru.godtools.tutorial.databinding.BakedInOnboardingOthersBinding
import org.cru.godtools.tutorial.databinding.BakedInOnboardingReadyBinding
import org.cru.godtools.tutorial.databinding.BakedInOnboardingToolsBinding
import org.cru.godtools.tutorial.databinding.BakedInOnboardingWelcomeBinding
import org.cru.godtools.tutorial.util.OnBoardingCallbacks

class BakedInOnBoardingPagerAdapter(val callbacks: OnBoardingCallbacks) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutID = OnBoardingPages.values()[position].layout
        val view = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(container.context),
            layoutID,
            container,
            false
        ).also { binding ->
            when (binding) {
                is BakedInOnboardingWelcomeBinding -> binding.callback = callbacks
                is BakedInOnboardingReadyBinding -> binding.callback = callbacks
                is BakedInOnboardingToolsBinding -> binding.callback = callbacks
                is BakedInOnboardingOthersBinding -> binding.callback = callbacks
                is BakedInOnboardingFinalBinding -> binding.callback = callbacks
            }
        }.root
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun getCount(): Int = OnBoardingPages.values().size

    private enum class OnBoardingPages(@LayoutRes val layout: Int) {
        WELCOME(R.layout.baked_in_onboarding_welcome),
        OTHERS(R.layout.baked_in_onboarding_others),
        TOOLS(R.layout.baked_in_onboarding_tools),
        READY(R.layout.baked_in_onboarding_ready),
        FINAL(R.layout.baked_in_onboarding_final)
    }
}
