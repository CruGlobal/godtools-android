package org.cru.godtools.article.aem.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.EXTRA_ARTICLE
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.fragment.AemArticleFragment
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.base.tool.activity.BaseToolActivity
import java.util.Locale

private const val TAG_MAIN_FRAGMENT = "mainFragment"

class AemArticleActivity : BaseSingleToolActivity(false) {
    // these properties should be treated as final and only set/modified in onCreate()
    private lateinit var articleUri: Uri

    private var article: Article? = null

    // region Lifecycle Events

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) {
            return
        }

        // finish now if we couldn't process the intent
        if (!processIntent()) {
            finish()
            return
        }

        setContentView(R.layout.activity_generic_tool_fragment)
        setupViewModel()
    }

    override fun onStart() {
        super.onStart()
        loadFragmentIfNeeded()
    }

    private fun onUpdateArticle(article: Article?) {
        this.article = article
        updateToolbarTitle()
        updateVisibilityState()
    }

    // endregion Lifecycle Events

    /**
     * @return true if the intent was successfully processed, otherwise return false
     */
    private fun processIntent(): Boolean {
        intent?.extras?.apply {
            articleUri = getParcelable(EXTRA_ARTICLE) ?: return false
            return true
        }

        return false
    }

    private fun setupViewModel() {
        val viewModel = ViewModelProviders.of(this).get(AemArticleViewModel::class.java)

        if (!viewModel.isArticleInitialized()) {
            viewModel.article = ArticleRoomDatabase.getInstance(this).articleDao().findLiveData(articleUri)
        }

        viewModel.article.observe(this, Observer<Article> { onUpdateArticle(it) })
    }

    override fun updateToolbarTitle() {
        title = article?.title ?: run {
            super.updateToolbarTitle()
            return
        }
    }

    override fun determineActiveToolState(): Int {
        val toolState = super.determineActiveToolState()
        return when {
            toolState != BaseToolActivity.STATE_LOADED -> toolState
            article?.content == null -> BaseToolActivity.STATE_LOADING
            else -> BaseToolActivity.STATE_LOADED
        }
    }

    @MainThread
    private fun loadFragmentIfNeeded() {
        // The fragment is already present
        if (supportFragmentManager.findFragmentByTag(TAG_MAIN_FRAGMENT) != null) return

        // load the fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, AemArticleFragment.newInstance(mTool, mLocale, articleUri), TAG_MAIN_FRAGMENT)
            .commit()
    }

    class AemArticleViewModel : ViewModel() {
        internal lateinit var article: LiveData<Article>

        internal fun isArticleInitialized() = ::article.isInitialized
    }

    companion object {
        @JvmStatic
        fun start(activity: Activity, toolCode: String, language: Locale, articleUri: Uri) {
            val extras = buildExtras(activity, toolCode, language).apply {
                putParcelable(EXTRA_ARTICLE, articleUri)
            }
            val intent = Intent(activity, AemArticleActivity::class.java).putExtras(extras)
            activity.startActivity(intent)
        }
    }
}
