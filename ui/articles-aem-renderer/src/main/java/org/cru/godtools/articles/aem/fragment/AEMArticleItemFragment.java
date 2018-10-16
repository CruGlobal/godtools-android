package org.cru.godtools.articles.aem.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.cru.godtools.article.aem.R;
import org.cru.godtools.article.aem.R2;
import org.cru.godtools.articles.aem.db.ArticleRoomDatabase;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Resource;
import org.cru.godtools.base.tool.fragment.BaseToolFragment;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

import butterknife.BindView;
import timber.log.Timber;

import static org.cru.godtools.articles.aem.Constants.EXTRA_ARTICLE;

public class AEMArticleItemFragment extends BaseToolFragment {

    private List<Resource> mResources;

    @BindView(R2.id.aem_article_web_view)
    WebView mWebView;
    private final AEMWebViewClient mWebViewClient = new AEMWebViewClient();

    // these properties should be treated as final and only set/modified in onCreate()
    @Nullable
    private /*final*/ Uri mArticleUri;

    @Nullable
    private Article mArticle;

    public static AEMArticleItemFragment newInstance(String tool, Locale locale, Uri articleKey) {

        AEMArticleItemFragment fragment = new AEMArticleItemFragment();
        Bundle args = new Bundle();
        populateArgs(args, tool, locale);
        args.putParcelable(EXTRA_ARTICLE, articleKey);
        fragment.setArguments(args);
        return fragment;
    }

    //region LifeCycle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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

    //endregion LifeCycle

    private void validateStartState() {
        if (mArticleUri == null) {
            throw new IllegalStateException("No article specified");
        }
    }

    private void setupViewModel() {
        final AemArticleViewModel viewModel = ViewModelProviders.of(this).get(AemArticleViewModel.class);

        if (viewModel.article == null) {
            ArticleRoomDatabase db = ArticleRoomDatabase.getInstance(requireContext());
            assert mArticleUri != null : "mArticleUri has to be non-null to reach this point";
            viewModel.article = db.articleDao().findLiveData(mArticleUri);
//            viewModel.resources = db.resourceDao().getAllLiveForArticle(mArticleUri);

        }

        viewModel.article.observe(this, this::onUpdateArticle);
//        viewModel.resources.observe(this, resources -> mResources = resources);
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

    //region WebClient

    private class AEMWebViewClient extends WebViewClient {
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(url);

            if (extension.isEmpty() || mResources == null) {
                return super.shouldInterceptRequest(view, url);
            }
            try {

                String mimeType;

                switch (extension) {
                    case "jpg":
                    case "png":
                    case "bmp":
                    case "gif":
                        mimeType = String.format("image/%s", "jpg".equals(extension) ? "jpeg" : extension);
                        return getResponseFromFile(mimeType, url);
                    case "css":
                        mimeType = "text/css";
                        return getResponseFromFile(mimeType, url);
                }
            } catch (FileNotFoundException | NoSuchElementException e) {
                Timber.d(e);
            }

            return super.shouldInterceptRequest(view, url);
        }

        private WebResourceResponse getResponseFromFile(@NonNull String mimeType, @NonNull String url)
                throws FileNotFoundException, NoSuchElementException {

//            Resource resource = Stream.of(mResources)
//                    .filter(r -> r.getUri().toString().equals(url)).findFirst().get();

            Resource resource = ArticleRoomDatabase.getInstance(requireActivity()).resourceDao()
                    .find(Uri.parse(url));

            if (resource == null || resource.getLocalFileName().isEmpty()) {
                throw new FileNotFoundException(String.format("No local file for %s", url));
            }

            FileInputStream inputStream = new FileInputStream(Objects.requireNonNull(
                    resource.getLocalFile(requireContext())));
            return returnWebResponse(mimeType, inputStream);
        }

        private WebResourceResponse returnWebResponse(String mimeType, InputStream stream) {
            return new WebResourceResponse(mimeType, "UTF-8", stream);
        }
    }
    //endregion WebClient

    public static class AemArticleViewModel extends ViewModel {
        LiveData<Article> article;
//        LiveData<List<Resource>> resources;
    }
}
