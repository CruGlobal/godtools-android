package org.cru.godtools.article.ui.articles

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.commit
import java.util.Locale
import org.cru.godtools.article.EXTRA_CATEGORY
import org.cru.godtools.article.R
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.ui.startAemArticleActivity
import org.cru.godtools.article.analytics.model.ArticlesAnalyticsScreenEvent
import org.cru.godtools.article.analytics.model.ArticlesCategoryAnalyticsScreenEvent
import org.cru.godtools.base.tool.activity.BaseArticleActivity
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.base.tool.databinding.ToolGenericFragmentActivityBinding

fun Activity.startArticlesActivity(toolCode: String, language: Locale, category: String?) {
    val extras = BaseSingleToolActivity.buildExtras(this, toolCode, language).apply {
        putString(EXTRA_CATEGORY, category)
    }
    Intent(this, ArticlesActivity::class.java)
        .putExtras(extras)
        .also { this.startActivity(it) }
}

class ArticlesActivity :
    BaseArticleActivity<ToolGenericFragmentActivityBinding>(R.layout.tool_generic_fragment_activity),
    ArticlesFragment.Callbacks {
    private val category: String? by lazy { intent?.extras?.getString(EXTRA_CATEGORY) }

    // region Lifecycle
    override fun onStart() {
        super.onStart()
        loadPrimaryFragmentIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        sendAnalyticsEvent()
    }

    override fun onArticleSelected(article: Article?) {
        article?.let { startAemArticleActivity(tool, locale, it.uri) }
    }
    // endregion Lifecycle

    private fun loadPrimaryFragmentIfNeeded() {
        with(supportFragmentManager) {
            if (primaryNavigationFragment != null) return

            commit {
                val fragment = ArticlesFragment(tool, locale, category)
                replace(R.id.frame, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
    }

    override fun updateToolbarTitle() {
        // try to use the Category Label for the title
        activeManifest
            ?.findCategory(category)
            ?.label?.text
            ?.let {
                title = it
                return
            }

        // otherwise default to the default toolbar title
        super.updateToolbarTitle()
    }

    private fun sendAnalyticsEvent() {
        when {
            category != null -> category?.let { ArticlesCategoryAnalyticsScreenEvent(tool, locale, it) }
            else -> ArticlesAnalyticsScreenEvent(tool, locale)
        }?.let { eventBus.post(it) }
    }
}
