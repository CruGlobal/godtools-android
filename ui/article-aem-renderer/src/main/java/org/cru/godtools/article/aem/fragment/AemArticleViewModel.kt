package org.cru.godtools.article.aem.fragment

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Article

class AemArticleViewModel(application: Application) : AndroidViewModel(application) {
    private val db = ArticleRoomDatabase.getInstance(application)

    val articleUri = MutableLiveData<Uri?>()
    private val article: LiveData<Article?> =
        articleUri.distinctUntilChanged().switchMap { db.articleDao().findLiveData(it) }

    // region WebView
    private var webView: WebView? = null
    private val webViewClient: ArticleWebViewClient = ArticleWebViewClient(application)

    fun getWebView(activity: Activity): WebView {
        webViewClient.updateActivity(activity)
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
