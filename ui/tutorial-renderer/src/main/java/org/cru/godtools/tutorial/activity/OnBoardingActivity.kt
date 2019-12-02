package org.cru.godtools.tutorial.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import org.cru.godtools.tutorial.adapter.OnboardingPagerAdapter
import org.cru.godtools.tutorial.R

class OnBoardingActivity : AppCompatActivity(),
    OnBoardingCallbacks {
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        val pagerAdapter = OnboardingPagerAdapter(this, supportFragmentManager)
        viewPager = findViewById(R.id.onboarding_viewpager)
        viewPager.adapter = pagerAdapter
    }

    // endregion lifecycle

    // region OnBoardingCallbacks
    override fun onNextClicked() {
        if (viewPager.currentItem < viewPager.childCount) {
            viewPager.currentItem = viewPager.currentItem + 1
        } else {
            finish()
        }
    }

    override fun onPreviousClicked() {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem = viewPager.currentItem - 1
        } else {
            finish()
        }
    }

    override fun onCloseClicked() {
        finish()
    }
    // endregion OnBoardingCallbacks
}

interface OnBoardingCallbacks {
    fun onNextClicked()
    fun onPreviousClicked()
    fun onCloseClicked()
}
