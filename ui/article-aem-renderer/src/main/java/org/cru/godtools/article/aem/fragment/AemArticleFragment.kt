package org.cru.godtools.article.aem.fragment

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.annotation.WorkerThread
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import butterknife.BindView
import com.karumi.weak.weak
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.R2
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.service.AemArticleManger
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.base.ui.util.openUrl
import splitties.fragmentargs.arg
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.ExecutionException

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

class AemArticleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = ArticleRoomDatabase.getInstance(application)

    val articleUri = MutableLiveData<Uri?>()
    private val article: LiveData<Article?> =
        articleUri.distinctUntilChanged().switchMap { db.articleDao().findLiveData(it) }

    // region WebView
    private var webView: WebView? = null
    private val webViewClient: ArticleWebViewClient = ArticleWebViewClient(application)

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
        article.observeForever { updateWebViewArticle(it) }
    }
    // endregion WebView Content
}

private class ArticleWebViewClient(context: Context) : WebViewClient() {
    var activity: Activity? by weak()
    private val resourceDao = ArticleRoomDatabase.getInstance(context).resourceDao()

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        activity?.openUrl(Uri.parse(url))
        return true
    }

    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        val uri = Uri.parse(url)
        if (uri.scheme == "data") return null
        return uri.getResponseFromFile(view.context) ?: notFoundResponse()
    }

    private fun notFoundResponse() = WebResourceResponse(null, null, null).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setStatusCodeAndReasonPhrase(HttpURLConnection.HTTP_NOT_FOUND, "Resource not available")
    }

    @WorkerThread
    private fun Uri.getResponseFromFile(context: Context): WebResourceResponse? {
        val resource = getResource(context) ?: return null
        val type = resource.contentType
        val data = resource.getData(context) ?: return null
        return WebResourceResponse(
            type?.let { "${type.type()}/${type.subtype()}" } ?: "application/octet-stream",
            type?.charset()?.name(), data
        )
    }

    private fun Uri.getResource(context: Context): Resource? {
        val resource = resourceDao.find(this) ?: return null
        if (resource.isDownloaded) return resource

        // attempt to download the file if we haven't downloaded it already
        try {
            // TODO: this may create a memory leak due to the call stack holding a reference to a WebView
            AemArticleManger.getInstance(context).enqueueDownloadResource(resource.uri, false).get()
        } catch (e: InterruptedException) {
            // propagate thread interruption
            Thread.currentThread().interrupt()
            return null
        } catch (e: ExecutionException) {
            Timber.tag(TAG).d(e.cause, "Error downloading resource when trying to render an article")
        }

        // refresh resource since we may have just downloaded it
        return resourceDao.find(this)
    }

    private fun Resource.getData(context: Context) =
        try {
            getInputStream(context)
        } catch (e: FileNotFoundException) {
            // the file wasn't found in the local cache directory. log the error and clear the local file state so
            // it is downloaded again.
            Timber.tag(TAG).e(e, "Missing cached version of: %s", uri)
            resourceDao.updateLocalFile(uri, null, null, null)
            null
        } catch (e: IOException) {
            Timber.tag(TAG).d(e, "Error opening local file")
            null
        }
}
