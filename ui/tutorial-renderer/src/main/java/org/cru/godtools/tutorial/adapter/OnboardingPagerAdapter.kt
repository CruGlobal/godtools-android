package org.cru.godtools.tutorial.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.cru.godtools.tutorial.fragment.OnboardingSlideFragment

class OnboardingPagerAdapter(
    val callbacks: org.cru.godtools.tutorial.activity.OnBoardingCallbacks,
    fm: FragmentManager
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return OnboardingSlideFragment.newInstance(position).apply {
            setCallback(callbacks)
        }
    }

    override fun getCount(): Int {
        return 4
    }
}
