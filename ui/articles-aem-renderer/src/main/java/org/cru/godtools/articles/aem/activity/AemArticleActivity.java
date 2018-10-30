package org.cru.godtools.articles.aem.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.cru.godtools.article.aem.R;
import org.cru.godtools.articles.aem.db.ArticleRoomDatabase;
import org.cru.godtools.articles.aem.fragment.AemArticleFragment;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity;

import java.util.Locale;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;

import static org.cru.godtools.articles.aem.Constants.EXTRA_ARTICLE;

public class AemArticleActivity extends BaseSingleToolActivity {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    // these properties should be treated as final and only set/modified in onCreate()
    @NonNull
    @SuppressWarnings("NullableProblems")
    /*final*/ ArticleRoomDatabase mAemDb;
    @Nullable
    private /*final*/ Uri mArticleUri;

    @Nullable
    private Article mArticle;

    // region Constructors and Initializers

    public static void start(@NonNull final Activity activity, @NonNull final String toolCode,
                             @NonNull final Locale language, @NonNull final Uri articleUri) {
        final Bundle extras = new Bundle();
        populateExtras(extras, toolCode, language);
        extras.putParcelable(EXTRA_ARTICLE, articleUri);
        final Intent intent = new Intent(activity, AemArticleActivity.class).putExtras(extras);
        activity.startActivity(intent);
    }

    public AemArticleActivity() {
        super(false);
    }

    // endregion Constructors and Initializers

    // region Lifecycle Events

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFinishing()) {
            return;
        }

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mArticleUri = extras.getParcelable(EXTRA_ARTICLE);
        }

        // finish now if this activity is in an invalid state
        if (!validStartState()) {
            finish();
            return;
        }

        mAemDb = ArticleRoomDatabase.getInstance(this);
        setContentView(R.layout.activity_generic_tool_fragment);
        setupViewModel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadFragmentIfNeeded();
    }

    void onUpdateArticle(@Nullable final Article article) {
        mArticle = article;
        updateToolbarTitle();
        updateVisibilityState();
    }

    // endregion Lifecycle Events

    private boolean validStartState() {
        return mArticleUri != null;
    }

    private void setupViewModel() {
        final AemArticleViewModel viewModel = ViewModelProviders.of(this).get(AemArticleViewModel.class);

        if (viewModel.article == null) {
            assert mArticleUri != null : "mArticleUri has to be non-null to reach this point";
            viewModel.article = mAemDb.articleDao().findLiveData(mArticleUri);
        }

        viewModel.article.observe(this, this::onUpdateArticle);
    }

    @Override
    protected void updateToolbarTitle() {
        if (mArticle != null) {
            setTitle(mArticle.title);
        } else {
            super.updateToolbarTitle();
        }
    }

    @Override
    protected int determineActiveToolState() {
        final int state = super.determineActiveToolState();
        if (state != STATE_LOADED) {
            return state;
        }
        return mArticle != null && mArticle.content != null ? STATE_LOADED : STATE_LOADING;
    }

    @MainThread
    private void loadFragmentIfNeeded() {
        final FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentByTag(TAG_MAIN_FRAGMENT) != null) {
            return; // The fragment is already present
        }

        assert mArticleUri != null : "mArticleUri has to be non-null to reach this point";
        fm.beginTransaction()
                .replace(R.id.frame, AemArticleFragment.newInstance(mTool, mLocale, mArticleUri), TAG_MAIN_FRAGMENT)
                .commit();
    }

    public static class AemArticleViewModel extends ViewModel {
        LiveData<Article> article;
    }
}
