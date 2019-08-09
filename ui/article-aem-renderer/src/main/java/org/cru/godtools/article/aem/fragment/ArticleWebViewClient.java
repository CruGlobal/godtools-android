package org.cru.godtools.article.aem.fragment;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.cru.godtools.article.aem.db.ArticleRoomDatabase;
import org.cru.godtools.article.aem.db.ResourceDao;
import org.cru.godtools.article.aem.model.Resource;
import org.cru.godtools.article.aem.service.AemArticleManger;
import org.cru.godtools.base.ui.util.WebUrlLauncher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import okhttp3.MediaType;
import timber.log.Timber;

class ArticleWebViewClient extends WebViewClient {
    private final Context mContext;
    @NonNull
    private final ArticleRoomDatabase mAemDb;
    @NonNull
    private Reference<Activity> mActivity = new WeakReference<>(null);

    ArticleWebViewClient(@NonNull final Context context) {
        mContext = context.getApplicationContext();
        mAemDb = ArticleRoomDatabase.Companion.getInstance(context);
    }

    void updateActivity(@Nullable final Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
        final Activity activity = mActivity.get();
        if (activity != null) {
            WebUrlLauncher.openUrl(activity, Uri.parse(url));
        }
        return true;
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(@NonNull final WebView view, @NonNull final String url) {
        final Uri uri = Uri.parse(url);
        if ("data".equals(uri.getScheme())) {
            return null;
        }

        final WebResourceResponse response = getResponseFromFile(view.getContext(), uri);
        if (response != null) {
            return response;
        }

        // we didn't have a response, return not found
        final WebResourceResponse notFound = new WebResourceResponse(null, null, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notFound.setStatusCodeAndReasonPhrase(HttpURLConnection.HTTP_NOT_FOUND, "Resource not available");
        }
        return notFound;
    }

    @Nullable
    @WorkerThread
    private WebResourceResponse getResponseFromFile(@NonNull final Context context, @NonNull final Uri uri) {
        final ResourceDao resourceDao = mAemDb.resourceDao();

        // find the referenced resource
        Resource resource = resourceDao.find(uri);
        if (resource == null) {
            return null;
        }

        // attempt to download the file if we haven't downloaded it already
        if (!resource.isDownloaded()) {
            try {
                // TODO: this may create a memory leak due to the call stack holding a reference to a WebView
                AemArticleManger.getInstance(context).enqueueDownloadResource(resource.getUri(), false).get();
            } catch (InterruptedException e) {
                // propagate thread interruption
                Thread.currentThread().interrupt();
                return null;
            } catch (ExecutionException e) {
                Timber.tag("AEMArticleFragment")
                        .d(e.getCause(), "Error downloading resource when trying to render an article");
            }

            // refresh resource since we may have just downloaded it
            resource = resourceDao.find(uri);
            if (resource == null) {
                return null;
            }
        }

        // get the data input stream
        InputStream data = null;
        try {
            data = resource.getInputStream(mContext);
        } catch (final FileNotFoundException e) {
            // the file wasn't found in the local cache directory. log the error and clear the local file state so
            // it is downloaded again.
            Timber.tag(AemArticleFragment.TAG)
                    .e(e, "Missing cached version of: %s", resource.getUri());
            resourceDao.updateLocalFile(resource.getUri(), null, null, null);
        } catch (final IOException e) {
            Timber.tag(AemArticleFragment.TAG)
                    .d(e, "Error opening local file");
        }
        if (data == null) {
            return null;
        }

        // return the response object
        final MediaType type = resource.getContentType();
        final String mimeType = type != null ? type.type() + "/" + type.subtype() : "application/octet-stream";
        final Charset encoding = type != null ? type.charset() : null;
        return new WebResourceResponse(mimeType, encoding != null ? encoding.name() : null, data);
    }
}
