package org.cru.godtools.article.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import org.ccci.gto.android.common.androidx.lifecycle.notNull
import org.ccci.gto.android.common.androidx.lifecycle.observeOnce
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.ui.startAemArticleActivity
import org.cru.godtools.article.ui.articles.ArticlesFragment
import org.cru.godtools.article.ui.categories.CategoriesFragment
import org.cru.godtools.article.ui.categories.CategorySelectedListener
import org.cru.godtools.base.DAGGER_HOST_CUSTOM_URI
import org.cru.godtools.base.SCHEME_GODTOOLS
import org.cru.godtools.base.tool.activity.BaseArticleActivity
import org.cru.godtools.tool.R
import org.cru.godtools.tool.databinding.ToolGenericFragmentActivityBinding
import org.cru.godtools.tool.model.Category
import org.cru.godtools.tool.model.Manifest

@AndroidEntryPoint
class ArticlesActivity :
    BaseArticleActivity<ToolGenericFragmentActivityBinding>(R.layout.tool_generic_fragment_activity),
    CategorySelectedListener,
    ArticlesFragment.Callbacks {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return
        if (savedInstanceState == null) trackToolOpen(tool, Manifest.Type.ARTICLE)
        setupFragments()
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

    // region Intent Processing
    @Inject
    @Named(DAGGER_HOST_CUSTOM_URI)
    internal lateinit var hostCustomUriScheme: String

    override fun processIntent(intent: Intent, savedInstanceState: Bundle?) {
        super.processIntent(intent, savedInstanceState)
        val data = intent.data?.normalizeScheme() ?: return
        val path = data.pathSegments ?: return

        when (intent.action) {
            Intent.ACTION_VIEW -> when {
                // Sample deep link: godtools://org.cru.godtools/tool/article/{tool}/{locale}
                data.isCustomUriDeepLink() -> {
                    dataModel.toolCode.value = path[2]
                    dataModel.locale.value = Locale.forLanguageTag(path[3])
                }
            }
        }
    }

    private fun Uri.isCustomUriDeepLink() = scheme == SCHEME_GODTOOLS &&
        hostCustomUriScheme.equals(host, true) && pathSegments.orEmpty().size >= 4 &&
        pathSegments?.getOrNull(0) == "tool" && pathSegments?.getOrNull(1) == "article"
    // endregion Intent Processing

    private fun updateToolbarTitle() {
        title = primaryNavigationFragmentTitle.orEmpty()
    }

    // region Fragments
    @MainThread
    private fun setupFragments() {
        // update the toolbar title when there is a change to the fragment back stack
        supportFragmentManager.addOnBackStackChangedListener { updateToolbarTitle() }

        // load the primaryNavigationFragment if one doesn't already exist
        dataModel.manifest.notNull().observeOnce(this) {
            if (supportFragmentManager.primaryNavigationFragment != null) return@observeOnce

            when (it.categories.size) {
                0, 1 -> showFragment(articlesFragment(it.categories.firstOrNull()))
                else -> showFragment(CategoriesFragment(tool, locale))
            }
        }
    }

    private val primaryNavigationFragmentTitle
        get() = when (val f = supportFragmentManager.primaryNavigationFragment) {
            is ArticlesFragment -> dataModel.manifest.value?.findCategory(f.category)?.label?.text
            else -> null
        }

    private fun showFragment(fragment: Fragment, addToBackStack: Boolean = false) = supportFragmentManager.commit {
        setReorderingAllowed(true)
        replace(R.id.frame, fragment)
        setPrimaryNavigationFragment(fragment)
        if (addToBackStack) addToBackStack(null) else runOnCommit { updateToolbarTitle() }
    }

    private fun articlesFragment(category: Category? = null) = ArticlesFragment(tool, locale, category?.id)
    // endregion Fragments
}
