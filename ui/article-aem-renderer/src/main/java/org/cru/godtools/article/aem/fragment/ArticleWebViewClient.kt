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
        return getResponseFromFile(view.context, uri) ?: notFoundResponse
    }

    @WorkerThread
    private fun getResponseFromFile(context: Context, uri: Uri): WebResourceResponse? {
        // attempt to download the file if we haven't downloaded it already
        var resource: Resource = resourceDao.find(uri) ?: return null
        if (!resource.isDownloaded) {
            try {
                // TODO: this may create a memory leak due to the call stack holding a reference to a WebView
                AemArticleManger.getInstance(context).enqueueDownloadResource(resource.uri, false).get()
            } catch (e: InterruptedException) {
                // propagate thread interruption
                Thread.currentThread().interrupt()
                return null
            } catch (e: ExecutionException) {
                Timber.tag(AemArticleFragment.TAG)
                    .d(e.cause, "Error downloading resource when trying to render an article")
            }

            // refresh resource since we may have just downloaded it
            resource = resourceDao.find(uri) ?: return null
        }

        // get the data input stream
        var data: InputStream? = null
        try {
            data = resource.getInputStream(context)
        } catch (e: FileNotFoundException) {
            // the file wasn't found in the local cache directory. log the error and clear the local file state so
            // it is downloaded again.
            Timber.tag(AemArticleFragment.TAG)
                .e(e, "Missing cached version of: %s", resource.uri)
            resourceDao.updateLocalFile(resource.uri, null, null, null)
        } catch (e: IOException) {
            Timber.tag(AemArticleFragment.TAG)
                .d(e, "Error opening local file")
        }

        if (data == null) {
            return null
        }

        // return the response object
        val type = resource.contentType
        val mimeType = type?.let { "${type.type()}/${type.subtype()}" } ?: "application/octet-stream"
        return WebResourceResponse(mimeType, type?.charset()?.name(), data)
    }
}
