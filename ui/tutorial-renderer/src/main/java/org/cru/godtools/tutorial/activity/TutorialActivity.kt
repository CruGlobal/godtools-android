package org.cru.godtools.tutorial.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.forEach
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import me.relex.circleindicator.CircleIndicator3
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.TutorialCallbacks
import org.cru.godtools.tutorial.TutorialPageFragment
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsScreenEvent

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

    override fun onResume() {
        super.onResume()
        trackScreenAnalytics()
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
    private var viewPager: ViewPager2? = null

    private fun setupViewPager() {
        viewPager = findViewById<ViewPager2>(R.id.tutorial_viewpager)?.also {
            it.adapter = TutorialPagerAdapter(this, pageSet.pages)
            it.setupAnalytics()
            it.setupMenuVisibility()
            it.setupIndicator()
        }
    }

    private fun ViewPager2.setupIndicator() {
        this@TutorialActivity.findViewById<CircleIndicator3>(R.id.on_boarding_indicator)?.let { indicator ->
            indicator.setViewPager(this)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) = updateIndicatorVisibility(indicator, position)
            })
            updateIndicatorVisibility(indicator)
        }
    }

    private fun ViewPager2.updateIndicatorVisibility(indicator: CircleIndicator3, page: Int = currentItem) {
        indicator.visibility = if (pageSet.pages[page].showIndicator) View.VISIBLE else View.GONE
    }
    // endregion ViewPager

    // region Analytics
    private fun ViewPager2.setupAnalytics() {
        registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = trackScreenAnalytics(position)
        })
    }

    private fun trackScreenAnalytics(page: Int? = viewPager?.currentItem) {
        if (page != null) mEventBus.post(TutorialAnalyticsScreenEvent(pageSet, page))
    }
    // endregion Analytics

    // region Menu
    private var menu: Menu? = null

    private fun ViewPager2.setupMenuVisibility() {
        registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = updateMenuVisibility(position)
        })
        updateMenuVisibility()
    }

    private fun ViewPager2.updateMenuVisibility(page: Int = currentItem) {
        val visible = pageSet.pages[page].showMenu
        menu?.forEach { it.isVisible = visible }
    }
    // endregion Menu

    // region TutorialCallbacks
    override fun nextPage() {
        viewPager?.apply {
            currentItem = (currentItem + 1).coerceAtMost(adapter?.itemCount ?: 0)
        }
    }

    override fun launchTraining() {
        startTutorialActivity(PageSet.TRAINING)
        finish()
    }

    override fun finishTutorial() = finish()

    override fun analyticsMore() {}

    override fun analyticsStart() {}
    // endregion TutorialCallbacks
}

internal class TutorialPagerAdapter(activity: FragmentActivity, private val pages: List<Page>) :
    FragmentStateAdapter(activity) {
    override fun getItemCount() = pages.size
    override fun createFragment(position: Int) = TutorialPageFragment(pages[position])
}
