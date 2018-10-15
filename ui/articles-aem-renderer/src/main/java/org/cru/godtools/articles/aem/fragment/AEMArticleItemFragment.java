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
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.cru.godtools.article.aem.R;
import org.cru.godtools.article.aem.R2;
import org.cru.godtools.articles.aem.db.ArticleRoomDatabase;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.base.tool.fragment.BaseToolFragment;

import java.util.Locale;

import butterknife.BindView;

import static org.cru.godtools.articles.aem.Constants.EXTRA_ARTICLE;

public class AEMArticleItemFragment extends BaseToolFragment {
    @BindView(R2.id.aem_article_web_view)
    WebView mAemWebView;
    private final AEMWebViewClient mWebViewClient = new AEMWebViewClient();

    // these properties should be treated as final and only set/modified in onCreate()
    @Nullable
    private /*final*/ Uri mArticleUri;

    @Nullable
    private Article mArticle;

    public static AEMArticleItemFragment newInstance(@NonNull final String tool, @NonNull final Locale locale,
                                                     @NonNull final Uri articleUri) {
        AEMArticleItemFragment fragment = new AEMArticleItemFragment();
        final Bundle args = new Bundle(3);
        populateArgs(args, tool, locale);
        args.putParcelable(EXTRA_ARTICLE, articleUri);
        fragment.setArguments(args);
        return fragment;
    }

    // region Lifecycle Events

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mArticleUri = args.getParcelable(EXTRA_ARTICLE);
        }

        validateStartState();

        setViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(
                R.layout.fragment_aem_article_item,
                container,
                false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAemWebView == null) {
            return;
        }
        mAemWebView.setWebViewClient(mWebViewClient);
    }

    // endregion Lifecycle Events

    private void validateStartState() {
        if (mArticleUri == null) {
            throw new IllegalStateException("No article specified");
        }
    }

    //region Article Data
    private void setViewModel() {
        mViewModel = ViewModelProviders.of(this).get(AemArticleWebViewModel.class);

        if (mViewModel.article == null) {
            ArticleRoomDatabase db = ArticleRoomDatabase.getInstance(requireContext());
            mViewModel.article = db.articleDao().findLiveData(mArticleUri);
        }
        mViewModel.article.observe(this, this::setArticle);
    }

    public void setArticle(Article article) {
        this.mArticle = article;

        setFragmentViews();
    }

    private void setFragmentViews() {
        if (mArticle != null && mArticle.content != null) {
            loadWebViewData();
            requireActivity().setTitle(mArticle.title);
        }
    }

    private void loadWebViewData() {
        if (mArticle == null || mAemWebView == null || mArticleUri == null) {
            return;
        }
        String data = getUpdatedHtml(mArticle.content);
        mAemWebView.loadDataWithBaseURL(mArticleUri.toString(), data,
                                        "text/html", null, null);
    }

    @NonNull
    private String getUpdatedHtml(String content) {
        StringBuilder builder = new StringBuilder(content);
        builder.insert(content.indexOf("<head>") + 7,
                "<style> img { max-width: 100%; } </style>");
        return builder.toString();
    }

    //endregion Article Data

    //region WebClient

    private class AEMWebViewClient extends WebViewClient {
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (url.endsWith(".jpg")) {
                //TODO: Load local Image WebResource
            } else if (url.endsWith(".css")) {
                //TODO: Load CSS WebResource
            }

            return super.shouldInterceptRequest(view, url);
        }
    }
    //endregion WebClient

    private AemArticleWebViewModel mViewModel;

    public static class AemArticleWebViewModel extends ViewModel {
        LiveData<Article> article;
    }
}
