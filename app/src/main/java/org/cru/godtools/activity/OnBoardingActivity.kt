package org.cru.godtools.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import org.cru.godtools.R
import org.cru.godtools.adapter.OnboardingPagerAdapter

class OnBoardingActivity : AppCompatActivity(), OnBoardingCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        val viewPager: ViewPager = findViewById(R.id.onboarding_viewpager)
        val pagerAdapter = OnboardingPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = pagerAdapter
    }

    // endregion lifecycle

    // region OnBoardingCallbacks
    override fun onNextClicked() {

    }

    override fun onPreviousClicked() {

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

