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
    private val pageSet get() = intent?.getSerializableExtra(ARG_PAGE_SET) as? PageSet ?: PageSet.DEFAULT

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tutorial_activity)
    }

    override fun onContentChanged() {
        super.onContentChanged()
        setupViewPager()
    }

    override fun onStart() {
        super.onStart()
        pageSet.feature?.let { Settings.getInstance(this).setFeatureDiscovered(it) }
    }
    // endregion Lifecycle

    // region ViewPager
    private var viewPager: ViewPager? = null

    private fun setupViewPager() {
        viewPager = findViewById<ViewPager>(R.id.tutorial_viewpager)?.also {
            it.adapter = TutorialPagerAdapter(pageSet.pages, this)
            it.setupIndicator()
        }
    }

    private fun ViewPager.setupIndicator() {
        findViewById<CircleIndicator>(R.id.on_boarding_indicator)?.let { indicator ->
            indicator.setViewPager(this)
            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    updateIndicatorVisibility(indicator, position)
                }
            })
            updateIndicatorVisibility(indicator)
        }
    }

    private fun ViewPager.updateIndicatorVisibility(indicator: CircleIndicator, page: Int = currentItem) {
        indicator.visibility = if (pageSet.pages[page].showIndicator) View.VISIBLE else View.GONE
    }
    // endregion ViewPager

    // region TutorialCallbacks
    override fun onNextClicked() {
        viewPager?.let { pager ->
            if (pager.currentItem < pager.adapter?.count ?: 0) {
                pager.currentItem = pager.currentItem + 1
            } else {
                finish()
            }
        }
    }

    override fun onPreviousClicked() {
        viewPager?.let { pager ->
            if (pager.currentItem > 0) {
                pager.currentItem = pager.currentItem - 1
            } else {
                finish()
            }
        }
    }

    override fun onCloseClicked() {
        finish()
    }

    override fun onOptInClicked() {
        startTutorialActivity(PageSet.TRAINING)
        finish()
    }
    // endregion TutorialCallbacks
}
