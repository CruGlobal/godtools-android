package org.cru.godtools.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.cru.godtools.activity.OnBoardingCallbacks
import org.cru.godtools.fragment.OnboardingSlideFragment

class OnboardingPagerAdapter(val callbacks: OnBoardingCallbacks, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return OnboardingSlideFragment.newInstance(position).apply {
            setCallback(callbacks)
        }
    }

    override fun getCount(): Int {
        return 1
    }
}
