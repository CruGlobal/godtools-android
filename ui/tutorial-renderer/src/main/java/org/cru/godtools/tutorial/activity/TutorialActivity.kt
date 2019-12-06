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
import org.cru.godtools.tutorial.adapter.TutorialPagerAdapter

import org.cru.godtools.tutorial.util.TutorialCallbacks
import org.cru.godtools.tutorial.util.TutorialState

class TutorialActivity : AppCompatActivity(), TutorialCallbacks {
    private lateinit var viewPager: ViewPager
    private lateinit var indicator: CircleIndicator
    private lateinit var state: TutorialState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        viewPager = findViewById(R.id.baked_in_viewpager)
        state = TutorialState.values()[intent.getIntExtra(ARG_STATE_INDEX, 0)]
        viewPager.adapter = TutorialPagerAdapter(this).also {
            setUpAdapterViews(it)
        }
        setupIndicator()
    }

    private fun setUpAdapterViews(it: TutorialPagerAdapter) {
        when (state) {
            TutorialState.BAKED_IN -> {
                Settings.getInstance(this).setFeatureDiscovered(Settings.FEATURE_BAKED_IN_TUTORIAL)
                it.pages = listOf(
                    R.layout.baked_in_tutorial_welcome,
                    R.layout.baked_in_tutorial_others,
                    R.layout.baked_in_tutorial_tools,
                    R.layout.baked_in_tutorial_ready,
                    R.layout.baked_in_tutorial_final
                )
            }
            TutorialState.OPT_IN -> {
                Settings.getInstance(this).setFeatureDiscovered(Settings.FEATURE_OPT_IN_TUTORIAL)
                it.pages = listOf(
                    R.layout.optin_tutorial_explore_slide,
                    R.layout.optin_tutorial_prepare_slide,
                    R.layout.optin_tutorial_try_slide,
                    R.layout.optin_tutorial_menu_slide
                )
            }
        }
    }

    private fun setupIndicator() {
        indicator = findViewById(R.id.on_boarding_indicator)
        indicator.setViewPager(viewPager)
        when (state) {
            TutorialState.BAKED_IN -> viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
            TutorialState.OPT_IN -> indicator.visibility = View.VISIBLE
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
        startOnBoardingActivity(TutorialState.OPT_IN)
        finish()
    }
    // endregion OnBoardingCallbacks

    // region Instantiate new Instance
    companion object {

        const val ARG_STATE_INDEX = "state_index"

        @JvmStatic
        fun Activity.startOnBoardingActivity(state: TutorialState) {
            val intent = Intent(this, TutorialActivity::class.java)
            intent.putExtra(ARG_STATE_INDEX, TutorialState.values().indexOf(state))
            startActivity(intent)
        }
    }
    // endregion Instantiate new Instance
}
