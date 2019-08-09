package org.cru.godtools.article.aem.fragment

import android.app.Activity
import android.app.Application
import android.content.Context
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
    private var mWebView: WebView? = null
    private val mWebViewClient: ArticleWebViewClient = ArticleWebViewClient(application)
    private var mContentUuid: String? = null

    fun getWebView(activity: Activity): WebView {
        if (mWebView == null) {
            val context: Context
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                context = ContextThemeWrapper(
                    activity.applicationContext,
                    R.style.Theme_GodTools_Tool_AppBar
                )
            } else {
                context = ContextThemeWrapper(activity.applicationContext, activity.theme)
            }

            mWebView = WebView(context)
            mWebView!!.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            mWebView!!.webViewClient = mWebViewClient
        }
        mWebViewClient.updateActivity(activity)

        return mWebView!!
    }

    fun updateWebViewArticle(article: Article?) {
        if (mWebView == null) {
            return
        }
        if (article?.content == null) {
            return
        }
        if (article.contentUuid == mContentUuid) return

        mWebView!!.loadDataWithBaseURL(
            article.uri.toString() + ".html", article.content,
            "text/html", null, null
        )
        mContentUuid = article.contentUuid
    }
}
