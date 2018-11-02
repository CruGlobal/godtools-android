package org.cru.godtools.article.aem.fragment;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.google.common.base.Objects;

import org.cru.godtools.article.aem.R;
import org.cru.godtools.article.aem.R2;
import org.cru.godtools.article.aem.db.ArticleRoomDatabase;
import org.cru.godtools.article.aem.db.ResourceDao;
import org.cru.godtools.article.aem.model.Article;
import org.cru.godtools.article.aem.model.Resource;
import org.cru.godtools.article.aem.service.AEMDownloadManger;
import org.cru.godtools.base.tool.fragment.BaseToolFragment;
import org.cru.godtools.base.ui.util.WebUrlLauncher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;
import okhttp3.MediaType;
import timber.log.Timber;

import static org.cru.godtools.article.aem.Constants.EXTRA_ARTICLE;

public class AemArticleFragment extends BaseToolFragment {
    private static final String TAG = "AemArticleFragment";

    @Nullable
    @BindView(R2.id.frame)
    FrameLayout mWebViewContainer;

    private AemArticleViewModel mViewModel;

    // these properties should be treated as final and only set/modified in onCreate()
    @Nullable
    private /*final*/ Uri mArticleUri;

    @Nullable
    private Article mArticle;

    public static AemArticleFragment newInstance(@NonNull final String tool, @NonNull final Locale locale,
                                                 @NonNull final Uri articleUri) {
        AemArticleFragment fragment = new AemArticleFragment();
        final Bundle args = new Bundle(3);
        populateArgs(args, tool, locale);
        args.putParcelable(EXTRA_ARTICLE, articleUri);
        fragment.setArguments(args);
        return fragment;
    }

    // region Lifecycle Events

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mArticleUri = args.getParcelable(EXTRA_ARTICLE);
        }

        validateStartState();

        setupViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_aem_article_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupWebView();
    }

    void onUpdateArticle(@Nullable final Article article) {
        mArticle = article;
        updateWebView();
    }

    @Override
    public void onDestroyView() {
        cleanupWebView();
        super.onDestroyView();
    }

    // endregion Lifecycle Events

    private void validateStartState() {
        if (mArticleUri == null) {
            throw new IllegalStateException("No article specified");
        }
    }

    private void setupViewModel() {
        mViewModel = ViewModelProviders.of(this).get(AemArticleViewModel.class);

        if (mViewModel.article == null) {
            assert mArticleUri != null : "mArticleUri has to be non-null to reach this point";
            mViewModel.article =
                    ArticleRoomDatabase.getInstance(requireContext()).articleDao().findLiveData(mArticleUri);
        }

        mViewModel.article.observe(this, this::onUpdateArticle);
    }

    // region WebView content

    private void setupWebView() {
        if (mWebViewContainer != null) {
            mWebViewContainer.addView(mViewModel.getWebView(requireActivity()));
        }
        updateWebView();
    }

    private void updateWebView() {
        mViewModel.updateWebViewArticle(mArticle);
    }

    private void cleanupWebView() {
        if (mWebViewContainer != null) {
            mWebViewContainer.removeView(mViewModel.getWebView(requireActivity()));
        }
    }

    // endregion WebView content

    private static class ArticleWebViewClient extends WebViewClient {
        private final Context mContext;
        @NonNull
        private final ArticleRoomDatabase mAemDb;
        @NonNull
        private Reference<Activity> mActivity = new WeakReference<>(null);

        ArticleWebViewClient(@NonNull final Context context) {
            mContext = context.getApplicationContext();
            mAemDb = ArticleRoomDatabase.getInstance(context);
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
            final WebResourceResponse response = getResponseFromFile(view.getContext(), Uri.parse(url));
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
                    AEMDownloadManger.getInstance(context).enqueueDownloadResource(resource.getUri(), false).get();
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
                Timber.tag(TAG)
                        .e(e, "Missing cached version of: %s", resource.getUri());
                resourceDao.updateLocalFile(resource.getUri(), null, null, null);
            } catch (final IOException e) {
                Timber.tag(TAG)
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

    public static class AemArticleViewModel extends AndroidViewModel {
        LiveData<Article> article;
        @Nullable
        private WebView mWebView;
        @NonNull
        private final ArticleWebViewClient mWebViewClient;
        @Nullable
        private String mContentUuid;

        public AemArticleViewModel(@NonNull final Application application) {
            super(application);
            mWebViewClient = new ArticleWebViewClient(application);
        }

        @NonNull
        WebView getWebView(@NonNull final Activity activity) {
            if (mWebView == null) {
                final Context context;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    context = new ContextThemeWrapper(activity.getApplicationContext(),
                                                      R.style.Theme_GodTools_Tool_AppBar);
                } else {
                    context = new ContextThemeWrapper(activity.getApplicationContext(), activity.getTheme());
                }

                mWebView = new WebView(context);
                mWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                    ViewGroup.LayoutParams.MATCH_PARENT));
                mWebView.setWebViewClient(mWebViewClient);
            }
            mWebViewClient.updateActivity(activity);

            return mWebView;
        }

        void updateWebViewArticle(@Nullable final Article article) {
            if (mWebView == null) {
                return;
            }
            if (article == null || article.getContent() == null) {
                return;
            }
            if (Objects.equal(article.getContentUuid(), mContentUuid)) {
                return;
            }

            mWebView.loadDataWithBaseURL(article.getUri().toString() + ".html", article.getContent(),
                    "text/html", null, null);
            mContentUuid = article.getContentUuid();
        }
    }
}
