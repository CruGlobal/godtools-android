package org.cru.godtools.article.aem.ui

import android.app.Activity
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.WorkerThread
import com.karumi.weak.weak
import java.io.FileNotFoundException
import java.io.IOException
import java.net.HttpURLConnection
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.cru.godtools.article.aem.db.ResourceDao
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.service.AemArticleManager
import org.cru.godtools.article.aem.util.AemFileSystem
import org.cru.godtools.base.ui.util.openUrl
import timber.log.Timber

private const val TAG = "ArticleWebViewClient"

internal class ArticleWebViewClient @Inject constructor(
    private val aemArticleManager: AemArticleManager,
    private val fs: AemFileSystem,
    private val resourceDao: ResourceDao
) : WebViewClient() {
    var activity: Activity? by weak()

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean {
        activity?.openUrl(request.url)
        return true
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
        val uri = request.url
        if (uri.scheme == "data") return null
        return uri.getResponseFromFile() ?: notFoundResponse()
    }

    private fun notFoundResponse() = WebResourceResponse(null, null, null).apply {
        setStatusCodeAndReasonPhrase(HttpURLConnection.HTTP_NOT_FOUND, "Resource not available")
    }

    @WorkerThread
    private fun Uri.getResponseFromFile(): WebResourceResponse? {
        val resource = findResource(this) ?: return null
        val type = resource.contentType
        val data = resource.getData() ?: return null
        return WebResourceResponse(
            type?.let { "${type.type}/${type.subtype}" } ?: "application/octet-stream",
            type?.charset()?.name(),
            data
        )
    }

    private fun findResource(uri: Uri) = runBlocking {
        val resource = resourceDao.find(uri) ?: return@runBlocking null
        if (resource.isDownloaded) return@runBlocking resource

        // attempt to download the file if we haven't downloaded it already
        // TODO: this may create a memory leak due to the call stack holding a reference to a WebView
        aemArticleManager.downloadResource(resource.uri, false)

        // refresh resource since we may have just downloaded it
        return@runBlocking resourceDao.find(uri)
    }

    private fun Resource.getData() = runBlocking {
        try {
            getInputStream(fs)
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
}
