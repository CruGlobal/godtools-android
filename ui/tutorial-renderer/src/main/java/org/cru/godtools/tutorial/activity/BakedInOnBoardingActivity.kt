package org.cru.godtools.tutorial.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import me.relex.circleindicator.CircleIndicator
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.adapter.BakedInOnBoardingPagerAdapter
import org.cru.godtools.tutorial.util.OnBoardingCallbacks

class BakedInOnBoardingActivity : AppCompatActivity(), OnBoardingCallbacks {
    private lateinit var viewPager: ViewPager
    private lateinit var indicator: CircleIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_baked_in_onboarding)
        viewPager = findViewById(R.id.baked_in_viewpager)
        viewPager.adapter = BakedInOnBoardingPagerAdapter(this)
        setupIndicator()
    }

    private fun setupIndicator() {
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                displayIndicator()
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                displayIndicator()
            }

            override fun onPageSelected(position: Int) {
                displayIndicator()
            }
        })
        indicator = findViewById(R.id.on_boarding_indicator)
        indicator.setViewPager(viewPager)
    }

    private fun displayIndicator() {
        // The Indicator is not displayed on the first Screen
        if (viewPager.currentItem != 0) {
            indicator.visibility = View.VISIBLE
        } else {
            indicator.visibility = View.GONE
        }
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

    override fun onOptInClicked() {
        startActivity(Intent(this, OptInOnBoardingActivity::class.java))
        finish()
    }
    // endregion OnBoardingCallbacks

}
