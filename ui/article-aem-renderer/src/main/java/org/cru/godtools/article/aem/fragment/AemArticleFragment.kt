package org.cru.godtools.article.aem.fragment

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import butterknife.BindView
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.R2
import org.cru.godtools.article.aem.db.ArticleDao
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.ui.ArticleWebViewClient
import org.cru.godtools.base.ui.fragment.BaseFragment
import splitties.fragmentargs.arg
import javax.inject.Inject

private const val TAG = "AemArticleFragment"

class AemArticleFragment() : BaseFragment<ViewDataBinding>() {
    constructor(articleUri: Uri) : this() {
        this.articleUri = articleUri
    }

    private var articleUri: Uri by arg()

    override val hasDataBinding get() = false

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_aem_article, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
    }

    override fun onDestroyView() {
        cleanupWebView()
        super.onDestroyView()
    }
    // endregion Lifecycle

    // region ViewModel
    private val viewModel: AemArticleViewModel by viewModels()

    private fun setupViewModel() {
        viewModel.articleUri.value = articleUri
    }
    // endregion ViewModel

    // region WebView content
    @JvmField
    @BindView(R2.id.frame)
    internal var webViewContainer: FrameLayout? = null

    private fun setupWebView() = webViewContainer?.addView(viewModel.getWebView(requireActivity()))

    private fun cleanupWebView() = webViewContainer?.removeView(viewModel.getWebView(requireActivity()))
    // endregion WebView content
}

internal class AemArticleViewModel @Inject constructor(
    application: Application,
    private val articleDao: ArticleDao,
    private val webViewClient: ArticleWebViewClient
) : AndroidViewModel(application) {
    val articleUri = MutableLiveData<Uri?>()
    private val article = articleUri.distinctUntilChanged().switchMap { articleDao.findLiveData(it) }

    // region WebView
    private var webView: WebView? = null

    fun getWebView(activity: Activity): WebView {
        webViewClient.activity = activity
        return webView ?: buildWebView(activity).also {
            webView = it
            updateWebViewArticle(article.value)
        }
    }

    private fun buildWebView(activity: Activity): WebView {
        val context = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                ContextThemeWrapper(activity.applicationContext, activity.theme)
            else -> ContextThemeWrapper(activity.applicationContext, R.style.Theme_GodTools_Tool_AppBar)
        }
        return WebView(context).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            it.webViewClient = webViewClient
        }
    }
    // endregion WebView

    // region WebView Content
    private var contentUuid: String? = null

    private fun updateWebViewArticle(article: Article?) {
        if (article?.content == null) return
        if (article.contentUuid == contentUuid) return
        if (webView == null) return

        webView?.loadDataWithBaseURL("${article.uri}.html", article.content, "text/html", null, null)
        contentUuid = article.contentUuid
    }

    init {
        article.observe(this) { updateWebViewArticle(it) }
    }
    // endregion WebView Content
}
