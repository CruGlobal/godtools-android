package org.cru.godtools.articles.aem.fragment;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import org.cru.godtools.articles.aem.db.AttachmentRepository;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.base.tool.fragment.BaseToolFragment;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;

public class AEMArticleItemFragment extends BaseToolFragment {

    public static final String TAG = "AEMArticleItemFragment";
    private static final String ARTICLE_KEY_TAG = "article_key";
    private Uri mArticleKey;
    private List<Attachment> mArticleAttachments;
    private ArticleRoomDatabase mRDb;
    private Article mArticle;
    private static final int MESSAGE_SET_ARTICLE = 101;

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

        mHandler.post(mWebViewRunnable);
    }
    //endregion LifeCycle

    //region Article Data
    private Article getArticleFromKey() {
        if (mRDb == null) {
            mRDb = ArticleRoomDatabase.getInstance(getContext().getApplicationContext());
        }
        return mRDb.articleDao().find(mArticleKey);
    }

    public void setArticle(Article article) {
        this.mArticle = article;

        setFragmentViews();
    }

    private void setFragmentViews() {
        if (mArticle.content != null) {
            loadWebViewData();
        }
        Objects.requireNonNull(getActivity()).setTitle(mArticle.title);
    }

    private void loadWebViewData() {
        StringBuilder builder = new StringBuilder(mArticle.content);
        builder.insert(mArticle.content.indexOf("<head>") + 7,
                "<style> img { max-width: 100%; } </style>");
        mAemWebView.loadDataWithBaseURL(mArticleKey.getHost(), builder.toString(),
                "text/html", null, null);
    }

    private void getArticleAttachments() {
        AttachmentRepository repository = new AttachmentRepository(getContext().getApplicationContext());
        mArticleAttachments = repository.getAttachmentsByArticle(mArticleKey.toString()).getValue();
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

    //region Runnable and Handler

    private Runnable mWebViewRunnable = new Runnable() {
        @Override
        public void run() {
            AsyncTask.execute(() -> {
                mArticle = getArticleFromKey();
                mHandler.sendEmptyMessage(MESSAGE_SET_ARTICLE);
                getArticleAttachments();
            });
        }
    };

    private AEMWebViewHandler mHandler = new AEMWebViewHandler(this);

    private static class AEMWebViewHandler extends Handler {
        WeakReference<AEMArticleItemFragment> mReference;

        AEMWebViewHandler(AEMArticleItemFragment fragment) {
            mReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            AEMArticleItemFragment fragment = mReference.get();

            if (fragment == null) {
                return;
            }

            if (msg.what == MESSAGE_SET_ARTICLE) {
                fragment.setFragmentViews();
            }
        }
    }
    //endregion Runnable and Handler
}
