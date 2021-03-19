package org.cru.godtools.article.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import org.ccci.gto.android.common.androidx.lifecycle.notNull
import org.ccci.gto.android.common.androidx.lifecycle.observeOnce
import org.cru.godtools.article.R
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.ui.startAemArticleActivity
import org.cru.godtools.article.ui.articles.ArticlesFragment
import org.cru.godtools.article.ui.categories.CategoriesFragment
import org.cru.godtools.article.ui.categories.CategorySelectedListener
import org.cru.godtools.base.tool.activity.BaseArticleActivity
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.base.tool.databinding.ToolGenericFragmentActivityBinding
import org.cru.godtools.xml.model.Category

fun Context.createArticlesIntent(toolCode: String, language: Locale): Intent {
    return Intent(this, ArticlesActivity::class.java)
        .putExtras(BaseSingleToolActivity.buildExtras(this, toolCode, language))
}

fun Activity.startArticlesActivity(toolCode: String, language: Locale) {
    startActivity(createArticlesIntent(toolCode, language))
}

@AndroidEntryPoint
class ArticlesActivity :
    BaseArticleActivity<ToolGenericFragmentActivityBinding>(R.layout.tool_generic_fragment_activity),
    CategorySelectedListener,
    ArticlesFragment.Callbacks {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        if (savedInstanceState == null) trackToolOpen(tool)
        loadPrimaryFragmentIfNeeded()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when {
        item.itemId == android.R.id.home && supportFragmentManager.backStackEntryCount > 0 -> {
            supportFragmentManager.popBackStack()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCategorySelected(category: Category?) = showFragment(articlesFragment(category), true)

    override fun onArticleSelected(article: Article?) {
        article?.let { startAemArticleActivity(tool, locale, it.uri) }
    }
    // endregion Lifecycle

    // region Fragments
    @MainThread
    private fun loadPrimaryFragmentIfNeeded() {
        dataModel.manifest.notNull().observeOnce(this) {
            if (supportFragmentManager.primaryNavigationFragment != null) return@observeOnce

            when (it.categories.size) {
                0, 1 -> showFragment(articlesFragment(it.categories.firstOrNull()))
                else -> showFragment(CategoriesFragment(tool, locale))
            }
        }
    }

    private fun showFragment(fragment: Fragment, addToBackStack: Boolean = false) = supportFragmentManager.commit {
        setReorderingAllowed(true)
        if (addToBackStack) addToBackStack(null)
        replace(R.id.frame, fragment)
        setPrimaryNavigationFragment(fragment)
    }

    private fun articlesFragment(category: Category? = null) = ArticlesFragment(tool, locale, category?.id)
    // endregion Fragments
}
