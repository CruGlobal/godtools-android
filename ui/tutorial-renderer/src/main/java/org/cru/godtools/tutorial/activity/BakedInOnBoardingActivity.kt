package org.cru.godtools.tutorial.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.adapter.BakedInOnBoardingPagerAdapter
import org.cru.godtools.tutorial.util.OnBoardingCallbacks

class BakedInOnBoardingActivity : AppCompatActivity(), OnBoardingCallbacks {
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_baked_in_onboarding)
        viewPager = findViewById(R.id.baked_in_viewpager)
        viewPager.adapter = BakedInOnBoardingPagerAdapter(this, supportFragmentManager)
    }

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
