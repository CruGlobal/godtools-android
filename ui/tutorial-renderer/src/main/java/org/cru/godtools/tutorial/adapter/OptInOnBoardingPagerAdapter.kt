package org.cru.godtools.tutorial.adapter

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.fragment.OptInOnBoardingSlideFragment
import org.cru.godtools.tutorial.util.OnBoardingCallbacks

class OptInOnBoardingPagerAdapter(val callbacks: OnBoardingCallbacks, fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return OptInOnBoardingSlideFragment.newInstance(OnBoardingPages.values()[position].layout).apply {
            setCallback(callbacks)
        }
    }

    override fun getCount(): Int {
        return OnBoardingPages.values().size
    }

    private enum class OnBoardingPages(@StringRes val layout: Int) {
        WATCH(R.layout.optin_onboarding_explore_slide),
        PREPARE(R.layout.optin_onboarding_prepare_slide),
        TRY(R.layout.optin_onboarding_try_slide),
        MENU(R.layout.optin_onboarding_menu_slide)
    }
}
