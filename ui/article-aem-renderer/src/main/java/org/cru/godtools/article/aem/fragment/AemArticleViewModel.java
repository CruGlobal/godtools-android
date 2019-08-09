package org.cru.godtools.article.aem.fragment;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.common.base.Objects;

import org.cru.godtools.article.aem.R;
import org.cru.godtools.article.aem.model.Article;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class AemArticleViewModel extends AndroidViewModel {
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
