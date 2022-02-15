package org.cru.godtools.article.aem.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.core.net.toUri
import androidx.fragment.app.commit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.map
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.cru.godtools.article.aem.EXTRA_ARTICLE
import org.cru.godtools.article.aem.PARAM_URI
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.analytics.model.ArticleAnalyticsScreenEvent
import org.cru.godtools.article.aem.fragment.AemArticleFragment
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.service.AemArticleManager
import org.cru.godtools.article.aem.util.removeExtension
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.tool.activity.BaseArticleActivity
import org.cru.godtools.base.tool.databinding.ToolGenericFragmentActivityBinding
import org.cru.godtools.base.ui.buildToolExtras

fun Activity.startAemArticleActivity(toolCode: String?, language: Locale, articleUri: Uri) {
    val extras = buildToolExtras(toolCode, language).apply {
        putParcelable(EXTRA_ARTICLE, articleUri)
    }
    Intent(this, AemArticleActivity::class.java)
        .putExtras(extras)
        .also { startActivity(it) }
}

@AndroidEntryPoint
class AemArticleActivity :
    BaseArticleActivity<ToolGenericFragmentActivityBinding>(R.layout.tool_generic_fragment_activity, false) {
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
        setupDataModel()
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

    private fun onUpdateArticle(article: Article?) {
        this.article = article
        updateToolbarTitle()
        sendAnalyticsEventIfNeededAndPossible()
        showNextFeatureDiscovery()
    }

    override fun onPause() {
        super.onPause()
        pendingAnalyticsEvent = false
    }
    // endregion Lifecycle

    // region Intent parsing
    /**
     * @return true if the intent was successfully processed, otherwise return false
     */
    private fun processIntent(): Boolean {
        articleUri = processDeepLink() ?: intent?.extras?.getParcelable(EXTRA_ARTICLE) ?: return false
        return true
    }

    private fun processDeepLink() = when {
        intent.isValidDeepLink() -> intent?.data?.getQueryParameter(PARAM_URI)?.toUri()?.removeExtension()
        else -> null
    }

    private fun Intent?.isValidDeepLink() =
        this != null && action == Intent.ACTION_VIEW && data?.isValidDeepLink() == true
    private fun Uri.isValidDeepLink() =
        (scheme == "http" || scheme == "https") && host == HOST_GODTOOLSAPP_COM && path == "/article/aem"
    // endregion Intent parsing

    private val articleDataModel: AemArticleViewModel by viewModels()

    private fun setupDataModel() {
        articleDataModel.articleUri.value = articleUri

        articleDataModel.article.observe(this) { onUpdateArticle(it) }
    }

    private fun sendAnalyticsEventIfNeededAndPossible() {
        if (!pendingAnalyticsEvent) return

        article?.let {
            eventBus.post(
                ArticleAnalyticsScreenEvent(it, manifestDataModel.toolCode.value, manifestDataModel.locale.value)
            )
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
    private val syncFinished = MutableLiveData(false)

    private fun syncData() {
        lifecycleScope.launch(Dispatchers.Main) {
            GlobalScope.launch {
                when {
                    intent.isValidDeepLink() -> aemArticleManager.downloadDeeplinkedArticle(articleUri)
                    else -> aemArticleManager.downloadArticle(articleUri, false)
                }
            }.join()
            syncFinished.value = true
        }
    }
    // endregion Sync logic

    // region Share Link logic
    override val shareLinkTitle get() = articleDataModel.article.value?.title ?: super.shareLinkTitle
    override val shareLinkUriLiveData by lazy {
        articleDataModel.article.map { (it?.shareUri ?: it?.canonicalUri)?.toString() }
    }
    // endregion Share Link logic

    override val activeToolLoadingStateLiveData by lazy {
        articleDataModel.article.combineWith(syncFinished) { article, syncFinished ->
            when {
                article?.content != null -> LoadingState.LOADED
                !syncFinished -> LoadingState.LOADING
                else -> LoadingState.NOT_FOUND
            }
        }.distinctUntilChanged()
    }

    @MainThread
    private fun loadFragmentIfNeeded() {
        with(supportFragmentManager) {
            if (primaryNavigationFragment != null) return

            commit {
                val fragment = AemArticleFragment()
                replace(R.id.frame, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
    }
}
