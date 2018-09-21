package org.cru.godtools.article.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import org.cru.godtools.article.R;
import org.cru.godtools.article.fragment.ArticlesFragment;
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity;

import java.util.Locale;

public class ArticlesActivity extends BaseSingleToolActivity {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";
    private static final String EXTRA_CATEGORY = "category";

    @Nullable
    private String mCategoryId = null;

    // region Initialization

    public static void start(@NonNull final Context context, @NonNull final String toolCode,
                             @NonNull final Locale language, @NonNull final String categoryID) {
        final Bundle args = new Bundle();
        populateExtras(args, toolCode, language);
        args.putString(EXTRA_CATEGORY, categoryID);
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
            mCategoryId = extras.getString(EXTRA_CATEGORY, mCategoryId);
        }

        setContentView(R.layout.activity_article_articles);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
    }

    // endregion Lifecycle Events

    private void loadInitialFragmentIfNeeded() {
        final FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentByTag(TAG_MAIN_FRAGMENT) == null) {
           fm.beginTransaction().replace(R.id.articles_frame, ArticlesFragment
                   .newInstance(mTool, mLocale, mCategoryId)).commit();
        }
    }
}
