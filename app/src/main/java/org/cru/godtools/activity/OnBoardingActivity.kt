package org.cru.godtools.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import org.cru.godtools.R
import org.cru.godtools.adapter.OnboardingPagerAdapter

class OnBoardingActivity : AppCompatActivity(), OnBoardingCallbacks {
    val viewPager by lazy { findViewById<ViewPager>(R.id.onboarding_viewpager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        val pagerAdapter = OnboardingPagerAdapter(this, supportFragmentManager)
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
