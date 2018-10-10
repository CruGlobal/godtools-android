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
    private ArticleRoomDatabase mRDb;
    private Article mArticle;

    @BindView(R2.id.aem_article_web_view)
    WebView mAemWebView;

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
        return inflater.inflate(R.layout.fragment_aem_article_item, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAemWebView.setWebViewClient(mWebViewClient);

        getArticleFromKey();
    }
    //endregion LifeCycle

    //region Article Data
    private void getArticleFromKey() {
        mViewModel = ViewModelProviders.of(this).get(AemArticleWebViewModel.class);

        if (!mViewModel.initialized) {
            ArticleRoomDatabase db = ArticleRoomDatabase.getInstance(requireActivity().getApplicationContext());
            mViewModel.getArticle = db.articleDao().liveFind(mArticleKey);
            mViewModel.getArticle.observe(this, this::setArticle);
            mViewModel.initialized = true;
        }
    }

    public void setArticle(Article article) {
        this.mArticle = article;

        setFragmentViews();
    }

    private void setFragmentViews() {
        if (mArticle.content != null) {
            loadWebViewData();
        }
        requireActivity().setTitle(mArticle.title);
    }

    private void loadWebViewData() {
        StringBuilder builder = new StringBuilder(mArticle.content);
        builder.insert(mArticle.content.indexOf("<head>") + 7,
                "<style> img { max-width: 100%; } </style>");
        mAemWebView.loadDataWithBaseURL("https://" + mArticleKey.getHost(), builder.toString(),
                "text/html", null, null);
    }

    //endregion Article Data

    //region WebClient
    private AEMWebViewClient mWebViewClient = new AEMWebViewClient();

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

        LiveData<Article> getArticle;

        boolean initialized = false;
    }
}
