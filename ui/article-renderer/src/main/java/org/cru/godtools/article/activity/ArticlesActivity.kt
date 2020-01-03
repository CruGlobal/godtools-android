package org.cru.godtools.article.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import org.cru.godtools.article.EXTRA_CATEGORY
import org.cru.godtools.article.R
import org.cru.godtools.article.aem.activity.startAemArticleActivity
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.analytics.model.ArticlesAnalyticsScreenEvent
import org.cru.godtools.article.analytics.model.ArticlesCategoryAnalyticsScreenEvent
import org.cru.godtools.article.fragment.ArticlesFragment
import org.cru.godtools.article.fragment.newArticlesFragment
import org.cru.godtools.base.tool.activity.BaseArticleActivity
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.xml.model.Text
import java.util.Locale

fun Activity.startArticlesActivity(toolCode: String, language: Locale, category: String?) {
    val extras = BaseSingleToolActivity.buildExtras(this, toolCode, language).apply {
        putString(EXTRA_CATEGORY, category)
    }
    Intent(this, ArticlesActivity::class.java)
        .putExtras(extras)
        .also { this.startActivity(it) }
}

class ArticlesActivity : BaseArticleActivity(false), ArticlesFragment.Callbacks {
    private val category: String? by lazy { intent?.extras?.getString(EXTRA_CATEGORY) }

    // region Lifecycle Events

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return

        setContentView(R.layout.activity_generic_tool_fragment)
    }

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

    // endregion Lifecycle Events

    private fun loadPrimaryFragmentIfNeeded() {
        with(supportFragmentManager) {
            if (primaryNavigationFragment != null) return

            commit {
                val fragment = newArticlesFragment(tool, locale, category)
                replace(R.id.frame, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
    }

    override fun updateToolbarTitle() {
        // try to use the Category Label for the title
        activeManifest
            ?.findCategory(category)?.orElse(null)
            ?.label?.let { Text.getText(it) }
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
        }?.let { mEventBus.post(it) }
    }
}
