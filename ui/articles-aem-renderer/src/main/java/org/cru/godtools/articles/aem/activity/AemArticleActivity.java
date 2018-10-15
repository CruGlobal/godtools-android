package org.cru.godtools.articles.aem.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import org.cru.godtools.article.aem.R;
import org.cru.godtools.articles.aem.fragment.AEMArticleItemFragment;
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity;

import java.util.Locale;

import static org.cru.godtools.articles.aem.Constants.EXTRA_ARTICLE;

public class AemArticleActivity extends BaseSingleToolActivity {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";
    private static final String EXTRA_ARTICLE_KEY = "extra_article_key";

    // these properties should be treated as final and only set/modified in onCreate()
    @Nullable
    private /*final*/ Uri mArticleUri;

    // region Constructors and Initializers

    public static void start(@NonNull final Context context, @NonNull final String toolCode,
                             @NonNull final Locale language, @NonNull final Uri articleUri) {
        final Bundle extras = new Bundle();
        populateExtras(extras, toolCode, language);
        extras.putParcelable(EXTRA_ARTICLE, articleUri);
        final Intent intent = new Intent(context, AemArticleActivity.class).putExtras(extras);
        context.startActivity(intent);
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

        setContentView(R.layout.activity_generic_fragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadFragmentIfNeeded();
    }

    // endregion Lifecycle Events

    private boolean validStartState() {
        return mArticleUri != null;
    }

    @MainThread
    private void loadFragmentIfNeeded() {
        final FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentByTag(TAG_MAIN_FRAGMENT) != null) {
            return; // The fragment is already present
        }

        fm.beginTransaction()
                .replace(R.id.frame, AEMArticleItemFragment.newInstance(mTool, mLocale, mArticleUri.toString()), TAG_MAIN_FRAGMENT)
                .commit();
    }
}
