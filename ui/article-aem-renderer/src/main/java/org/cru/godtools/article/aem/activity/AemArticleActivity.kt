package org.cru.godtools.article.aem.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.common.util.concurrent.ListenableFuture
import org.ccci.gto.android.common.util.MainThreadExecutor
import org.ccci.gto.android.common.util.WeakTask
import org.cru.godtools.article.aem.EXTRA_ARTICLE
import org.cru.godtools.article.aem.PARAM_URI
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.analytics.model.ArticleAnalyticsScreenEvent
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.fragment.AemArticleFragment
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.service.AemArticleManager
import org.cru.godtools.article.aem.util.removeExtension
import org.cru.godtools.base.tool.activity.BaseArticleActivity
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import org.cru.godtools.base.tool.activity.BaseToolActivity
import java.util.Locale

private const val TAG_MAIN_FRAGMENT = "mainFragment"

fun Activity.startAemArticleActivity(toolCode: String?, language: Locale, articleUri: Uri) {
    val extras = BaseSingleToolActivity.buildExtras(this, toolCode, language).apply {
        putParcelable(EXTRA_ARTICLE, articleUri)
    }
    Intent(this, AemArticleActivity::class.java)
        .putExtras(extras)
        .also { startActivity(it) }
}

class AemArticleActivity : BaseArticleActivity(false) {
    // these properties should be treated as final and only set/modified in onCreate()
    private lateinit var articleUri: Uri

    private lateinit var syncTask: ListenableFuture<Boolean>
    private var article: Article? = null

    private var pendingAnalyticsEvent = false

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

        syncData()
        setContentView(R.layout.activity_generic_tool_fragment)
        setupViewModel()
    }

    override fun onStart() {
        super.onStart()
        loadFragmentIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        pendingAnalyticsEvent = true
        sendAnalyticsEventIfNeededAndPossible()
    }

    internal fun onSyncTaskFinished() {
        updateVisibilityState()
    }

    private fun onUpdateArticle(article: Article?) {
        this.article = article
        updateToolbarTitle()
        updateShareMenuItem()
        updateVisibilityState()
        sendAnalyticsEventIfNeededAndPossible()
    }

    override fun onPause() {
        super.onPause()
        pendingAnalyticsEvent = false
    }

    // endregion Lifecycle Events

    /**
     * @return true if the intent was successfully processed, otherwise return false
     */
    private fun processIntent(): Boolean {
        articleUri = processDeepLink()
                ?: intent?.extras?.getParcelable(EXTRA_ARTICLE)
                ?: return false

        return true
    }

    private fun processDeepLink(): Uri? {
        return when {
            isValidDeepLink() -> intent?.data?.getQueryParameter(PARAM_URI)?.toUri()?.removeExtension()
            else -> null
        }
    }

    private fun isValidDeepLink(): Boolean {
        return intent?.action == Intent.ACTION_VIEW &&
                intent.data?.run {
                    (scheme == "http" || scheme == "https") &&
                            host == "godtoolsapp.com" &&
                            path == "/article/aem"
                } == true
    }

    private fun setupViewModel() {
        val viewModel = ViewModelProvider(this).get(AemArticleViewModel::class.java)

        if (!viewModel.isArticleInitialized()) {
            viewModel.article = ArticleRoomDatabase.getInstance(this).articleDao().findLiveData(articleUri)
        }

        viewModel.article.observe(this, Observer { onUpdateArticle(it) })
    }

    private fun sendAnalyticsEventIfNeededAndPossible() {
        if (!pendingAnalyticsEvent) return

        article?.let {
            eventBus.post(ArticleAnalyticsScreenEvent(it, mTool, mLocale))
            pendingAnalyticsEvent = false
        }
    }

    override fun updateToolbarTitle() {
        title = article?.title ?: run {
            super.updateToolbarTitle()
            return
        }
    }

    private fun syncData() {
        AemArticleManager.getInstance(this).let { manager ->
            syncTask = when {
                isValidDeepLink() -> manager.downloadDeeplinkedArticle(articleUri)
                else -> manager.downloadArticle(articleUri, false)
            }
            syncTask.addListener(WeakTask(this, WeakTask.Task { it.onSyncTaskFinished() }), MainThreadExecutor())
        }
    }

    // region Share Link logic

    override fun hasShareLinkUri(): Boolean = article?.canonicalUri != null

    override fun getShareLinkTitle(): String? = article?.title ?: super.getShareLinkTitle()

    override fun getShareLinkUri(): String? = article?.shareUri?.toString() ?: article?.canonicalUri?.toString()

    // endregion Share Link logic

    override fun determineActiveToolState(): Int {
        return when {
            article?.content != null -> BaseToolActivity.STATE_LOADED
            !this::syncTask.isInitialized -> BaseToolActivity.STATE_LOADING
            !syncTask.isDone -> BaseToolActivity.STATE_LOADING
            else -> BaseToolActivity.STATE_NOT_FOUND
        }
    }

    @MainThread
    private fun loadFragmentIfNeeded() {
        // The fragment is already present
        if (supportFragmentManager.findFragmentByTag(TAG_MAIN_FRAGMENT) != null) return

        // load the fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame, AemArticleFragment(articleUri), TAG_MAIN_FRAGMENT)
            .commit()
    }

    class AemArticleViewModel : ViewModel() {
        internal lateinit var article: LiveData<Article?>

        internal fun isArticleInitialized() = ::article.isInitialized
    }
}
