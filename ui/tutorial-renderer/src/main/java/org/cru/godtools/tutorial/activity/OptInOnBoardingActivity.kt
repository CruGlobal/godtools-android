package org.cru.godtools.tutorial.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import me.relex.circleindicator.CircleIndicator
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.adapter.OnBoardingPagerAdapter
import org.cru.godtools.tutorial.util.OnBoardingCallbacks

class OptInOnBoardingActivity : AppCompatActivity(),
    OnBoardingCallbacks {
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_optin_onboarding)
        viewPager = findViewById(R.id.onboarding_viewpager)
        viewPager.adapter = OnBoardingPagerAdapter(this).also {
            it.onBoardingPagesLayout = listOf(
                R.layout.optin_onboarding_explore_slide,
                R.layout.optin_onboarding_prepare_slide,
                R.layout.optin_onboarding_try_slide,
                R.layout.optin_onboarding_menu_slide
            )
            it.notifyDataSetChanged()
        }
        val indicator: CircleIndicator = findViewById(R.id.on_boarding_indicator)
        indicator.setViewPager(viewPager)
    }

    // endregion lifecycle

    // region OnBoardingCallbacks
    override fun onNextClicked() {
        if (viewPager.currentItem < viewPager.adapter?.count ?: 0) {
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

    override fun onOptInClicked() {} // Not used in this Activity
    // endregion OnBoardingCallbacks
}
