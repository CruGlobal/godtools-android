package org.cru.godtools.article.aem.fragment

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.WorkerThread
import com.karumi.weak.weakVar
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.service.AemArticleManger
import org.cru.godtools.base.ui.util.WebUrlLauncher
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.concurrent.ExecutionException

private const val TAG = "ArticleWebViewClient"

private val notFoundResponse
    get() = WebResourceResponse(null, null, null).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setStatusCodeAndReasonPhrase(HttpURLConnection.HTTP_NOT_FOUND, "Resource not available")
    }

internal class ArticleWebViewClient(context: Context) : WebViewClient() {
    var activity: Activity? by weakVar()
    private val resourceDao = ArticleRoomDatabase.getInstance(context).resourceDao()

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        activity?.let { WebUrlLauncher.openUrl(it, Uri.parse(url)) }
        return true
    }

    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        val uri = Uri.parse(url)
        if (uri.scheme == "data") return null
        return uri.getResponseFromFile(view.context) ?: notFoundResponse
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

    private fun Resource.getData(context: Context): InputStream? {
        try {
            return getInputStream(context)
        } catch (e: FileNotFoundException) {
            // the file wasn't found in the local cache directory. log the error and clear the local file state so
            // it is downloaded again.
            Timber.tag(TAG).e(e, "Missing cached version of: %s", uri)
            resourceDao.updateLocalFile(uri, null, null, null)
        } catch (e: IOException) {
            Timber.tag(TAG).d(e, "Error opening local file")
        }
        return null
    }
}
