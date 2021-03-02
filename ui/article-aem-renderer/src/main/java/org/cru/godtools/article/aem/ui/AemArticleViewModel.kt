package org.cru.godtools.article.aem.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.webkit.WebView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.db.ArticleDao

@HiltViewModel
internal class AemArticleViewModel @Inject constructor(
    private val articleDao: ArticleDao,
    private val webViewClient: ArticleWebViewClient
) : ViewModel() {
    val articleUri = MutableLiveData<Uri>()
    val article = articleUri.distinctUntilChanged().switchMap { articleDao.findLiveData(it) }

    // region WebView
    @SuppressLint("StaticFieldLeak")
    private lateinit var webView: WebView

    fun getWebView(activity: Activity): WebView {
        if (!::webView.isInitialized) buildWebView(activity)
        webViewClient.activity = activity
        return webView
    }

    private fun buildWebView(activity: Activity) {
        val context = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                ContextThemeWrapper(activity.applicationContext, activity.theme)
            else -> ContextThemeWrapper(activity.applicationContext, R.style.Theme_GodTools_Tool_AppBar)
        }

        webView = WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        webView.webViewClient = webViewClient

        article.observe(this) {
            val content = it?.content ?: return@observe
            if (it.contentUuid == webView.getTag(R.id.contentUuid)) return@observe
            webView.loadDataWithBaseURL("${it.uri}.html", content, "text/html", null, null)
            webView.setTag(R.id.contentUuid, it.contentUuid)
        }
    }
    // endregion WebView
}
