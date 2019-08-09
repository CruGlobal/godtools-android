package org.cru.godtools.article.aem.fragment

import android.app.Activity
import android.app.Application
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.model.Article

class AemArticleViewModel(application: Application) : AndroidViewModel(application) {
    var article: LiveData<Article>? = null

    private var webView: WebView? = null
    private val webViewClient: ArticleWebViewClient = ArticleWebViewClient(application)
    private var contentUuid: String? = null

    fun getWebView(activity: Activity): WebView {
        webViewClient.updateActivity(activity)
        return webView ?: buildWebView(activity).also { webView = it }
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

    fun updateWebViewArticle(article: Article?) {
        if (article?.content == null) return
        if (article.contentUuid == contentUuid) return
        if (webView == null) return

        webView?.loadDataWithBaseURL("${article.uri}.html", article.content, "text/html", null, null)
        contentUuid = article.contentUuid
    }
}
