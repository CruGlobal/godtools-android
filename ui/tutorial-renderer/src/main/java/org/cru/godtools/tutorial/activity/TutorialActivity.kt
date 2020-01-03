package org.cru.godtools.tutorial.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.forEach
import androidx.viewpager.widget.ViewPager
import me.relex.circleindicator.CircleIndicator
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.activity.BaseActivity
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

class TutorialActivity : BaseActivity(), TutorialCallbacks {
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

    override fun onSetupActionBar() {
        super.onSetupActionBar()
        setupAppBar()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        pageSet.menu?.let { menuInflater.inflate(it, menu) }
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        super.onStart()
        pageSet.feature?.let { Settings.getInstance(this).setFeatureDiscovered(it) }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        viewPager?.updateMenuVisibility()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.onboarding_action_skip -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    // endregion Lifecycle

    private fun setupAppBar() {
        mActionBar?.apply {
            setDisplayShowHomeEnabled(false)
            setDisplayHomeAsUpEnabled(pageSet.showUpNavigation)
            setDisplayShowTitleEnabled(false)
        }
    }

    // region ViewPager
    private var viewPager: ViewPager? = null

    private fun setupViewPager() {
        viewPager = findViewById<ViewPager>(R.id.tutorial_viewpager)?.also {
            it.adapter = TutorialPagerAdapter(supportFragmentManager, pageSet.pages)
            it.setupMenuVisibility()
            it.setupIndicator()
        }
    }

    private fun ViewPager.setupIndicator() {
        this@TutorialActivity.findViewById<CircleIndicator>(R.id.on_boarding_indicator)?.let { indicator ->
            indicator.setViewPager(this)
            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) = updateIndicatorVisibility(indicator, position)
            })
            updateIndicatorVisibility(indicator)
        }
    }

    private fun ViewPager.updateIndicatorVisibility(indicator: CircleIndicator, page: Int = currentItem) {
        indicator.visibility = if (pageSet.pages[page].showIndicator) View.VISIBLE else View.GONE
    }
    // endregion ViewPager

    // region Menu
    private var menu: Menu? = null

    private fun ViewPager.setupMenuVisibility() {
        addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) = updateMenuVisibility(position)
        })
        updateMenuVisibility()
    }

    private fun ViewPager.updateMenuVisibility(page: Int = currentItem) {
        val visible = pageSet.pages[page].showMenu
        menu?.forEach { it.isVisible = visible }
    }
    // endregion Menu

    // region TutorialCallbacks
    override fun nextPage() {
        viewPager?.apply {
            currentItem = (currentItem + 1).coerceAtMost(adapter?.count ?: 0)
        }
    }

    override fun launchTraining() {
        startTutorialActivity(PageSet.TRAINING)
        finish()
    }

    override fun finishTutorial() = finish()
    // endregion TutorialCallbacks
}
