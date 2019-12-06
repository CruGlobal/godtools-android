package org.cru.godtools.tutorial.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import me.relex.circleindicator.CircleIndicator
import org.cru.godtools.base.Settings
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.adapter.TutorialPagerAdapter
import org.cru.godtools.tutorial.util.TutorialCallbacks

private const val ARG_PAGE_SET = "pageSet"

fun Activity.startTutorialActivity(pageSet: PageSet = PageSet.DEFAULT) {
    Intent(this, TutorialActivity::class.java)
        .putExtra(ARG_PAGE_SET, pageSet)
        .also { startActivity(it) }
}

class TutorialActivity : AppCompatActivity(), TutorialCallbacks {
    private lateinit var viewPager: ViewPager
    private lateinit var indicator: CircleIndicator

    private val pageSet get() = intent?.getSerializableExtra(ARG_PAGE_SET) as? PageSet ?: PageSet.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        viewPager = findViewById(R.id.baked_in_viewpager)
        viewPager.adapter = TutorialPagerAdapter(pageSet.pages.toList(), this).also {
            setUpAdapterViews(it)
        }
        setupIndicator()
    }

    private fun setUpAdapterViews(it: TutorialPagerAdapter) {
        when (pageSet) {
            PageSet.BAKED_IN -> {
                Settings.getInstance(this).setFeatureDiscovered(Settings.FEATURE_BAKED_IN_TUTORIAL)
            }
            PageSet.OPT_IN -> {
                Settings.getInstance(this).setFeatureDiscovered(Settings.FEATURE_OPT_IN_TUTORIAL)
            }
        }
    }

    private fun setupIndicator() {
        indicator = findViewById(R.id.on_boarding_indicator)
        indicator.setViewPager(viewPager)
        when (pageSet) {
            PageSet.BAKED_IN -> viewPager.addOnPageChangeListener(object :
                ViewPager.OnPageChangeListener {
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
            PageSet.OPT_IN -> indicator.visibility = View.VISIBLE
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
        startTutorialActivity(PageSet.OPT_IN)
        finish()
    }
    // endregion OnBoardingCallbacks
}
