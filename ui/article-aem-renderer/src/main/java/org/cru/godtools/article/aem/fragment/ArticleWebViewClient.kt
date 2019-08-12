package org.cru.godtools.article.aem.fragment

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.WorkerThread
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.service.AemArticleManger
import org.cru.godtools.base.ui.util.WebUrlLauncher
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.util.concurrent.ExecutionException

internal class ArticleWebViewClient(context: Context) : WebViewClient() {
    private val mContext: Context = context.applicationContext
    private val mAemDb: ArticleRoomDatabase = ArticleRoomDatabase.getInstance(context)
    private var mActivity: Reference<Activity> = WeakReference(null)

    fun updateActivity(activity: Activity?) {
        mActivity = WeakReference(activity)
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val activity = mActivity.get()
        if (activity != null) {
            WebUrlLauncher.openUrl(activity, Uri.parse(url))
        }
        return true
    }

    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
        val uri = Uri.parse(url)
        if ("data" == uri.scheme) {
            return null
        }

        val response = getResponseFromFile(view.context, uri)
        if (response != null) {
            return response
        }

        // we didn't have a response, return not found
        val notFound = WebResourceResponse(null, null, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notFound.setStatusCodeAndReasonPhrase(HttpURLConnection.HTTP_NOT_FOUND, "Resource not available")
        }
        return notFound
    }

    @WorkerThread
    private fun getResponseFromFile(context: Context, uri: Uri): WebResourceResponse? {
        val resourceDao = mAemDb.resourceDao()

        // find the referenced resource
        var resource: Resource? = resourceDao.find(uri) ?: return null

        // attempt to download the file if we haven't downloaded it already
        if (!resource!!.isDownloaded) {
            try {
                // TODO: this may create a memory leak due to the call stack holding a reference to a WebView
                AemArticleManger.getInstance(context).enqueueDownloadResource(resource.uri, false).get()
            } catch (e: InterruptedException) {
                // propagate thread interruption
                Thread.currentThread().interrupt()
                return null
            } catch (e: ExecutionException) {
                Timber.tag("AEMArticleFragment")
                    .d(e.cause, "Error downloading resource when trying to render an article")
            }

            // refresh resource since we may have just downloaded it
            resource = resourceDao.find(uri)
            if (resource == null) {
                return null
            }
        }

        // get the data input stream
        var data: InputStream? = null
        try {
            data = resource.getInputStream(mContext)
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
        val mimeType = if (type != null) type.type() + "/" + type.subtype() else "application/octet-stream"
        val encoding = type?.charset()
        return WebResourceResponse(mimeType, encoding?.name(), data)
    }
}
