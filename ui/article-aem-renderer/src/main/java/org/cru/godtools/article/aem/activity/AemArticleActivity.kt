package org.cru.godtools.article.aem.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.core.net.toUri
import androidx.fragment.app.commit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import com.google.common.util.concurrent.ListenableFuture
import org.ccci.gto.android.common.util.MainThreadExecutor
import org.ccci.gto.android.common.util.WeakTask
import org.cru.godtools.article.aem.EXTRA_ARTICLE
import org.cru.godtools.article.aem.PARAM_URI
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.analytics.model.ArticleAnalyticsScreenEvent
import org.cru.godtools.article.aem.db.ArticleDao
import org.cru.godtools.article.aem.fragment.AemArticleFragment
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.service.AemArticleManager
import org.cru.godtools.article.aem.util.removeExtension
import org.cru.godtools.base.tool.activity.BaseArticleActivity
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity
import java.util.Locale
import javax.inject.Inject

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

    private var article: Article? = null

    private var pendingAnalyticsEvent = false

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return

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
        showNextFeatureDiscovery()
    }

    override fun onPause() {
        super.onPause()
        pendingAnalyticsEvent = false
    }
    // endregion Lifecycle

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

    private val dataModel: AemArticleActivityDataModel by viewModels()

    private fun setupViewModel() {
        dataModel.articleUri.value = articleUri
        dataModel.article.observe(this, Observer { onUpdateArticle(it) })
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

    // region Sync logic
    @Inject
    internal lateinit var aemArticleManager: AemArticleManager
    private lateinit var syncTask: ListenableFuture<Boolean>

    private fun syncData() {
        syncTask = when {
            isValidDeepLink() -> aemArticleManager.downloadDeeplinkedArticle(articleUri)
            else -> aemArticleManager.downloadArticle(articleUri, false)
        }
        syncTask.addListener(WeakTask(this, WeakTask.Task { it.onSyncTaskFinished() }), MainThreadExecutor())
    }
    // endregion Sync logic

    // region Share Link logic
    override fun hasShareLinkUri() = article?.canonicalUri != null
    override val shareLinkTitle get() = article?.title ?: super.shareLinkTitle
    override val shareLinkUri get() = article?.shareUri?.toString() ?: article?.canonicalUri?.toString()
    // endregion Share Link logic

    override fun determineActiveToolState(): Int {
        return when {
            article?.content != null -> STATE_LOADED
            !this::syncTask.isInitialized -> STATE_LOADING
            !syncTask.isDone -> STATE_LOADING
            else -> STATE_NOT_FOUND
        }
    }

    @MainThread
    private fun loadFragmentIfNeeded() {
        with(supportFragmentManager) {
            if (primaryNavigationFragment != null) return

            commit {
                val fragment = AemArticleFragment(articleUri)
                replace(R.id.frame, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
    }
}

class AemArticleActivityDataModel @Inject internal constructor(private val articleDao: ArticleDao) : ViewModel() {
    internal val articleUri = MutableLiveData<Uri>()

    internal val article = articleUri.distinctUntilChanged().switchMap { articleDao.findLiveData(it) }
}
