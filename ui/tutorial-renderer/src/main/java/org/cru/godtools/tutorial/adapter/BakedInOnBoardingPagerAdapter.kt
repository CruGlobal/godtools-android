package org.cru.godtools.tutorial.adapter

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.fragment.BakedInOnBoardingFragment
import org.cru.godtools.tutorial.util.OnBoardingCallbacks

class BakedInOnBoardingPagerAdapter(
    val callbacks: OnBoardingCallbacks,
    fragmentManager: FragmentManager
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment =
        BakedInOnBoardingFragment.newInstance(OnBoardingPages.values()[position].layout).also {
            it.setCallback(callbacks)
        }

    override fun getCount(): Int = OnBoardingPages.values().size

    private enum class OnBoardingPages(@StringRes val layout: Int) {
        WELCOME(R.layout.baked_in_onboarding_welcome),
        OTHERS(R.layout.baked_in_onboarding_others),
        TOOLS(R.layout.baked_in_onboarding_tools),
        READY(R.layout.baked_in_onboarding_ready),
        FINAL(R.layout.baked_in_onboarding_final)
    }
}
