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

public class AEMArticleItemFragment extends BaseToolFragment {

    private static final String ARTICLE_KEY_TAG = "article_key";
    private Uri mArticleKey;
    private Article mArticle;

    @BindView(R2.id.aem_article_web_view)
    WebView mAemWebView;

    private AEMWebViewClient mWebViewClient = new AEMWebViewClient();

    public static AEMArticleItemFragment newInstance(String tool, Locale locale, String articleKey) {

        AEMArticleItemFragment fragment = new AEMArticleItemFragment();
        Bundle args = new Bundle();
        populateArgs(args, tool, locale);
        args.putString(ARTICLE_KEY_TAG, articleKey);
        fragment.setArguments(args);
        return fragment;
    }

    //region LifeCycle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mArticleKey = Uri.parse(args.getString(ARTICLE_KEY_TAG));
        }
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
        setViewModel();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAemWebView == null || mViewModel == null) {
            return;
        }
        mAemWebView.setWebViewClient(mWebViewClient);
        mViewModel.article.observe(this, this::setArticle);
    }

    //endregion LifeCycle

    //region Article Data
    private void setViewModel() {
        mViewModel = ViewModelProviders.of(this).get(AemArticleWebViewModel.class);

        if (mViewModel.article == null) {
            ArticleRoomDatabase db = ArticleRoomDatabase.getInstance(requireContext());
            mViewModel.article = db.articleDao().liveFind(mArticleKey);
        }
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
        if (mArticle == null || mAemWebView == null || mArticleKey == null) {
            return;
        }
        String data = getUpdatedHtml(mArticle.content);
        mAemWebView.loadDataWithBaseURL(mArticleKey.toString(), data,
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
