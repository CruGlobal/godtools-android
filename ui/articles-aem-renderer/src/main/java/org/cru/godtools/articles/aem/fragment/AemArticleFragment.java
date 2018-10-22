package org.cru.godtools.articles.aem.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.cru.godtools.article.aem.R;
import org.cru.godtools.article.aem.R2;
import org.cru.godtools.articles.aem.db.ArticleRoomDatabase;
import org.cru.godtools.articles.aem.db.ResourceDao;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Resource;
import org.cru.godtools.articles.aem.service.AEMDownloadManger;
import org.cru.godtools.base.tool.fragment.BaseToolFragment;
import org.cru.godtools.base.ui.util.WebUrlLauncher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import okhttp3.MediaType;
import timber.log.Timber;

import static org.cru.godtools.articles.aem.Constants.EXTRA_ARTICLE;

public class AemArticleFragment extends BaseToolFragment {
    private static final String TAG = "AemArticleFragment";

    @BindView(R2.id.aem_article_web_view)
    WebView mWebView;
    private final ArticleWebViewClient mWebViewClient = new ArticleWebViewClient();

    // these properties should be treated as final and only set/modified in onCreate()
    @NonNull
    @SuppressWarnings("NullableProblems")
    /*final*/ ArticleRoomDatabase mAemDb;
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
        mAemDb = ArticleRoomDatabase.getInstance(requireContext());

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
        setActivityTitle();
        updateWebView();
    }

    // endregion Lifecycle Events

    private void validateStartState() {
        if (mArticleUri == null) {
            throw new IllegalStateException("No article specified");
        }
    }

    private void setupViewModel() {
        final AemArticleViewModel viewModel = ViewModelProviders.of(this).get(AemArticleViewModel.class);

        if (viewModel.article == null) {
            assert mArticleUri != null : "mArticleUri has to be non-null to reach this point";
            viewModel.article = mAemDb.articleDao().findLiveData(mArticleUri);
        }

        viewModel.article.observe(this, this::onUpdateArticle);
    }

    private void setActivityTitle() {
        if (mArticle != null) {
            requireActivity().setTitle(mArticle.title);
        }
    }

    // region WebView content

    private void setupWebView() {
        if (mWebView != null) {
            mWebView.setWebViewClient(mWebViewClient);
            updateWebView();
        }
    }

    private void updateWebView() {
        if (mWebView != null) {
            if (mArticle != null && mArticle.content != null) {
                assert mArticleUri != null : "mArticleUri has to be non-null to reach this point";
                final String content = injectCss(mArticle.content);
                mWebView.loadDataWithBaseURL(mArticleUri.toString() + ".html", content, null, null, null);
            }
        }
    }

    @NonNull
    private String injectCss(@NonNull final String content) {
        final StringBuilder builder = new StringBuilder(content);
        builder.insert(content.indexOf("<head>") + 7,
                "<style> img { max-width: 100%; } </style>");
        return builder.toString();
    }

    // endregion WebView content

    private class ArticleWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            WebUrlLauncher.openUrl(requireActivity(), Uri.parse(url));
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
                data = resource.getInputStream(requireContext());
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

    public static class AemArticleViewModel extends ViewModel {
        LiveData<Article> article;
    }
}
