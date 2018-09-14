package org.cru.godtools.article.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import org.cru.godtools.article.fragment.ArticlesFragment;
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity;
import org.cru.godtools.article.R;
import java.util.Locale;

public class ArticlesActivity extends BaseSingleToolActivity {

    private static final String CATEGORY_KEY = "category_key";
    private String mCategoryID;

    //region Initialization
    public static void start(@NonNull final Context context, @NonNull final String toolCode,
                                @NonNull final Locale language, @NonNull final String categoryID) {
        Bundle args = new Bundle();
        populateExtras(args, toolCode, language);
        args.putString(CATEGORY_KEY, categoryID);
        Intent intent = new Intent(context, ArticlesActivity.class).putExtras(args);
        context.startActivity(intent);
    }

    public ArticlesActivity() {
        super(true);
    }
    //endregion

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_articles);
        if (getIntent().hasExtra(CATEGORY_KEY)) {
            mCategoryID = getIntent().getStringExtra(CATEGORY_KEY);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
    }

    private void loadInitialFragmentIfNeeded() {
        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentByTag(ArticlesFragment.TAG) == null) {
           fm.beginTransaction().replace(R.id.articles_frame, ArticlesFragment
                   .newInstance(mTool, mLocale, mCategoryID != null ? mCategoryID : "")).commit();
        }
    }
}
