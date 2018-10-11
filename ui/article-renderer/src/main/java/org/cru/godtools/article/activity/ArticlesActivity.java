package org.cru.godtools.article.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import org.cru.godtools.article.R;
import org.cru.godtools.article.fragment.ArticlesFragment;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity;

import java.util.Locale;

import timber.log.Timber;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class ArticlesActivity extends BaseSingleToolActivity implements ArticlesFragment.Callbacks {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";
    private static final String EXTRA_CATEGORY = "category";

    @Nullable
    private String mCategory = null;

    // region Initialization

    public static void start(@NonNull final Context context, @NonNull final String toolCode,
                             @NonNull final Locale language, @NonNull final String category) {
        final Bundle args = new Bundle();
        populateExtras(args, toolCode, language);
        args.putString(EXTRA_CATEGORY, category);
        final Intent intent = new Intent(context, ArticlesActivity.class).putExtras(args);
        context.startActivity(intent);
    }

    public ArticlesActivity() {
        super(false);
    }

    // endregion

    // region Lifecycle Events

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFinishing()) {
            return;
        }

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mCategory = extras.getString(EXTRA_CATEGORY, mCategory);
        }

        setContentView(R.layout.activity_generic_fragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
    }

    @Override
    public void onArticleSelected(@Nullable final Article article) {
        Timber.tag("ArticlesActivity")
                .d("Article selected: %s", article != null ? article.title : null);
    }

    // endregion Lifecycle Events

    private void loadInitialFragmentIfNeeded() {
        final FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(TAG_MAIN_FRAGMENT) != null) {
            return;
        }

        assert mTool != null : "if mTool was null the activity would have already finished";
        fm.beginTransaction()
                .replace(R.id.frame, ArticlesFragment.newInstance(mTool, mLocale, mCategory), TAG_MAIN_FRAGMENT)
                .commit();
    }

    // region Up Navigation

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        assert mTool != null : "mTool has to be non-null for this activity to even be running";
        final Intent intent = super.getSupportParentActivityIntent();

        // populate the CategoriesActivity intent
        if (intent != null) {
            intent.addFlags(FLAG_ACTIVITY_SINGLE_TOP | FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(CategoriesActivity.populateExtras(new Bundle(), mTool, mLocale));
        }

        return intent;
    }

    // endregion Up Navigation
}
