package org.cru.godtools.articles.aem.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.cru.godtools.article.aem.R;
import org.cru.godtools.articles.aem.fragment.AemArticleFragment;
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity;

import java.util.Locale;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import static org.cru.godtools.articles.aem.Constants.EXTRA_ARTICLE;

public class AemArticleActivity extends BaseSingleToolActivity {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    // these properties should be treated as final and only set/modified in onCreate()
    @Nullable
    private /*final*/ Uri mArticleUri;

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

        assert mArticleUri != null : "mArticleUri has to be non-null to reach this point";
        fm.beginTransaction()
                .replace(R.id.frame, AemArticleFragment.newInstance(mTool, mLocale, mArticleUri), TAG_MAIN_FRAGMENT)
                .commit();
    }
}
