package org.cru.godtools.article.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.transaction
import org.cru.godtools.article.EXTRA_CATEGORY
import org.cru.godtools.article.R
import org.cru.godtools.article.aem.activity.startAemArticleActivity
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.fragment.ArticlesFragment
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.xml.model.Text
import java.util.Locale

private const val TAG_MAIN_FRAGMENT = "mainFragment"

fun Activity.startArticlesActivity(toolCode: String, language: Locale, category: String?) {
    val extras = BaseSingleToolActivity.buildExtras(this, toolCode, language).apply {
        putString(EXTRA_CATEGORY, category)
    }
    Intent(this, ArticlesActivity::class.java)
        .putExtras(extras)
        .also { this.startActivity(it) }
}

class ArticlesActivity : BaseSingleToolActivity(false), ArticlesFragment.Callbacks {
    private val category: String? by lazy { intent?.extras?.getString(EXTRA_CATEGORY) }

    // region Lifecycle Events

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return

        setContentView(R.layout.activity_generic_tool_fragment)
    }

    override fun onStart() {
        super.onStart()
        loadInitialFragmentIfNeeded()
    }

    override fun onArticleSelected(article: Article?) {
        article?.let { startAemArticleActivity(tool, locale, it.uri) }
    }

    // endregion Lifecycle Events

    private fun loadInitialFragmentIfNeeded() {
        supportFragmentManager?.apply {
            if (findFragmentByTag(TAG_MAIN_FRAGMENT) == null) {
                transaction {
                    replace(R.id.frame, ArticlesFragment.newInstance(tool, locale, category), TAG_MAIN_FRAGMENT)
                }
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
}
