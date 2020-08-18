package org.cru.godtools.article.aem.ui

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.WorkerThread
import com.karumi.weak.weak
import java.io.FileNotFoundException
import java.io.IOException
import java.net.HttpURLConnection
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import org.cru.godtools.article.aem.db.ResourceDao
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.service.AemArticleManager
import org.cru.godtools.base.ui.util.openUrl
import timber.log.Timber

private const val TAG = "ArticleWebViewClient"

internal class ArticleWebViewClient @Inject constructor(
    private val aemArticleManager: AemArticleManager,
    private val resourceDao: ResourceDao
) : WebViewClient() {
    var activity: Activity? by weak()

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
        val resource = getResource() ?: return null
        val type = resource.contentType
        val data = resource.getData(context) ?: return null
        return WebResourceResponse(
            type?.let { "${type.type()}/${type.subtype()}" } ?: "application/octet-stream",
            type?.charset()?.name(), data
        )
    }

    private fun Uri.getResource(): Resource? {
        val resource = resourceDao.find(this) ?: return null
        if (resource.isDownloaded) return resource

        // attempt to download the file if we haven't downloaded it already
        try {
            // TODO: this may create a memory leak due to the call stack holding a reference to a WebView
            aemArticleManager.enqueueDownloadResource(resource.uri, false).get()
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
