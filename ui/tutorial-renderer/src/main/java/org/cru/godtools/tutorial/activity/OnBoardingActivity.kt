package org.cru.godtools.tutorial.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import me.relex.circleindicator.CircleIndicator
import org.cru.godtools.base.Settings
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.adapter.OnBoardingPagerAdapter
import org.cru.godtools.tutorial.util.OnBoardingCallbacks
import org.cru.godtools.tutorial.util.OnBoardingState
import org.cru.godtools.tutorial.util.OnBoardingState.BAKED_IN
import org.cru.godtools.tutorial.util.OnBoardingState.OPT_IN

class OnBoardingActivity : AppCompatActivity(), OnBoardingCallbacks {
    private lateinit var viewPager: ViewPager
    private lateinit var indicator: CircleIndicator
    private lateinit var state: OnBoardingState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_baked_in_onboarding)
        viewPager = findViewById(R.id.baked_in_viewpager)
        state = OnBoardingState.values()[intent.getIntExtra(ARG_STATE_INDEX, 0)]
        viewPager.adapter = OnBoardingPagerAdapter(this).also {
            setUpAdapterViews(it)
        }
        setupIndicator()
    }

    private fun setUpAdapterViews(it: OnBoardingPagerAdapter) {
        when (state) {
            BAKED_IN -> {
                Settings.getInstance(this).setFeatureDiscovered(Settings.FEATURE_BAKED_IN_TUTORIAL)
                it.onBoardingPagesLayout = listOf(
                    R.layout.baked_in_onboarding_welcome,
                    R.layout.baked_in_onboarding_others,
                    R.layout.baked_in_onboarding_tools,
                    R.layout.baked_in_onboarding_ready,
                    R.layout.baked_in_onboarding_final
                )
            }
            OPT_IN -> {
                Settings.getInstance(this).setFeatureDiscovered(Settings.FEATURE_OPT_IN_TUTORIAL)
                it.onBoardingPagesLayout = listOf(
                    R.layout.optin_onboarding_explore_slide,
                    R.layout.optin_onboarding_prepare_slide,
                    R.layout.optin_onboarding_try_slide,
                    R.layout.optin_onboarding_menu_slide
                )
            }
        }
        it.notifyDataSetChanged()
    }

    private fun setupIndicator() {
        indicator = findViewById(R.id.on_boarding_indicator)
        indicator.setViewPager(viewPager)
        when (state) {
            BAKED_IN -> viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) {
                    displayIndicator()
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    displayIndicator()
                }

                override fun onPageSelected(position: Int) {
                    displayIndicator()
                }
            })
            OPT_IN -> indicator.visibility = View.VISIBLE
        }
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

    override fun onOptInClicked() {
        startOnBoardingActivity(OPT_IN)
        finish()
    }
    // endregion OnBoardingCallbacks

    // region Instantiate new Instance
    companion object {

        const val ARG_STATE_INDEX = "state_index"

        @JvmStatic
        fun Activity.startOnBoardingActivity(state: OnBoardingState) {
            val intent = Intent(this, OnBoardingActivity::class.java)
            intent.putExtra(ARG_STATE_INDEX, OnBoardingState.values().indexOf(state))
            startActivity(intent)
        }
    }
    // endregion Instantiate new Instance
}
