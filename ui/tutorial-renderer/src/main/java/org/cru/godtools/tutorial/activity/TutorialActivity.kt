package org.cru.godtools.tutorial.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.forEach
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import me.relex.circleindicator.CircleIndicator3
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.dashboard.Page as DashboardPage
import org.cru.godtools.base.ui.startArticlesActivity
import org.cru.godtools.base.ui.startDashboardActivity
import org.cru.godtools.base.ui.util.openUrl
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.TutorialCallbacks
import org.cru.godtools.tutorial.TutorialPageFragment
import org.cru.godtools.tutorial.analytics.model.ACTION_TUTORIAL_ONBOARDING_FINISH
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsScreenEvent
import org.cru.godtools.tutorial.databinding.TutorialActivityBinding

private const val ARG_PAGE_SET = "pageSet"
private const val ARG_FRMT_ARGS = "formatArgs"

fun Context.buildTutorialActivityIntent(pageSet: PageSet, formatArgs: Bundle? = null) =
    Intent(this, TutorialActivity::class.java)
        .putExtra(ARG_PAGE_SET, pageSet)
        .putExtra(ARG_FRMT_ARGS, formatArgs)

fun Activity.startTutorialActivity(pageSet: PageSet, stringArgs: Bundle? = null) =
    startActivity(buildTutorialActivityIntent(pageSet, stringArgs))

@AndroidEntryPoint
class TutorialActivity : BaseActivity<TutorialActivityBinding>(), TutorialCallbacks {
    private val pageSet get() = intent?.getSerializableExtra(ARG_PAGE_SET) as? PageSet ?: PageSet.DEFAULT
    private val pages get() = pageSet.pagesFor(Locale.getDefault())

    // region Lifecycle
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
        pageSet.feature?.let { settings.setFeatureDiscovered(it) }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        binding.pages.updateMenuVisibility()
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
        R.id.action_live_share_skip, R.id.action_tips_skip -> {
            setResult(RESULT_OK)
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
    // endregion Lifecycle

    override fun inflateBinding() = TutorialActivityBinding.inflate(layoutInflater)
    override val toolbar get() = binding.appbar

    private fun setupAppBar() {
        supportActionBar?.apply {
            setDisplayShowHomeEnabled(false)
            setDisplayHomeAsUpEnabled(pageSet.showUpNavigation)
            setDisplayShowTitleEnabled(false)
        }
    }

    // region ViewPager
    private fun setupViewPager() {
        binding.pages.apply {
            adapter = TutorialPagerAdapter(this@TutorialActivity, pages, intent?.getBundleExtra(ARG_FRMT_ARGS))
            setupAnalytics()
            setupMenuVisibility()
            setupIndicator()
        }
    }

    private fun ViewPager2.setupIndicator() {
        binding.progressIndicator.let { indicator ->
            indicator.setViewPager(this)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) = updateIndicatorVisibility(indicator, position)
            })
            updateIndicatorVisibility(indicator)
        }
    }

    private fun ViewPager2.updateIndicatorVisibility(indicator: CircleIndicator3, page: Int = currentItem) {
        indicator.visibility = if (pages[page].showIndicator) View.VISIBLE else View.GONE
    }
    // endregion ViewPager

    // region Analytics
    private fun ViewPager2.setupAnalytics() {
        registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = trackScreenAnalytics(position)
        })
    }

    private fun trackScreenAnalytics(pagePos: Int = binding.pages.currentItem) {
        eventBus.post(TutorialAnalyticsScreenEvent(pageSet, pages.getOrNull(pagePos), pagePos, deviceLocale))
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
        val visible = pages[page].showMenu
        menu?.forEach { it.isVisible = visible }
    }
    // endregion Menu

    // region TutorialCallbacks
    override fun nextPage() {
        with(binding.pages) {
            currentItem = (currentItem + 1).coerceAtMost(adapter?.itemCount ?: 0)
        }
    }

    override fun onTutorialAction(view: View) {
        when (view.id) {
            R.id.action_onboarding_watch_video -> {
                // TODO: this should launch in-app if possible
                openUrl(Uri.parse("https://www.youtube.com/watch?v=RvhZ_wuxAgE"))
            }
            R.id.action_onboarding_launch_articles -> {
                // TODO: we need to launch in whichever language makes sense for the current system/primary locale
                startArticlesActivity("es", Locale.ENGLISH)
                finish()
            }
            R.id.action_onboarding_launch_lessons -> {
                startDashboardActivity(DashboardPage.LESSONS)
                finish()
            }
            R.id.action_onboarding_launch_tools -> {
                startDashboardActivity(DashboardPage.FAVORITE_TOOLS)
                finish()
            }
            R.id.action_onboarding_finish -> {
                eventBus.post(TutorialAnalyticsActionEvent(ACTION_TUTORIAL_ONBOARDING_FINISH))
                finish()
            }
            R.id.action_live_share_finish, R.id.action_training_finish, R.id.action_tips_finish -> {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
    // endregion TutorialCallbacks

    override fun supportNavigateUpTo(upIntent: Intent) {
        setResult(RESULT_CANCELED)
        finish()
    }
}

internal class TutorialPagerAdapter(
    activity: FragmentActivity,
    private val pages: List<Page>,
    private val formatArgs: Bundle? = null
) : FragmentStateAdapter(activity) {
    override fun getItemCount() = pages.size
    override fun createFragment(position: Int) = TutorialPageFragment(pages[position], formatArgs)
}
