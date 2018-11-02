package org.cru.godtools.article.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.cru.godtools.article.R;
import org.cru.godtools.article.aem.activity.AemArticleActivity;
import org.cru.godtools.article.aem.model.Article;
import org.cru.godtools.article.fragment.ArticlesFragment;
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity;
import org.cru.godtools.xml.model.Category;
import org.cru.godtools.xml.model.Manifest;
import org.cru.godtools.xml.model.Text;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import static org.cru.godtools.article.Constants.EXTRA_CATEGORY;

public class ArticlesActivity extends BaseSingleToolActivity implements ArticlesFragment.Callbacks {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    @Nullable
    private String mCategory = null;

    // region Initialization

    public static void start(@NonNull final Activity activity, @NonNull final String toolCode,
                             @NonNull final Locale language, @Nullable final String category) {
        final Bundle args = buildExtras(activity, toolCode, language);
        args.putString(EXTRA_CATEGORY, category);
        final Intent intent = new Intent(activity, ArticlesActivity.class).putExtras(args);
        activity.startActivity(intent);
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

        setContentView(R.layout.activity_generic_tool_fragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
    }

    @Override
    public void onArticleSelected(@Nullable final Article article) {
        if (article != null) {
            AemArticleActivity.start(this, mTool, mLocale, article.getUri());
        }
    }

    // endregion Lifecycle Events

    private void loadInitialFragmentIfNeeded() {
        final FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(TAG_MAIN_FRAGMENT) != null) {
            return;
        }

        fm.beginTransaction()
                .replace(R.id.frame, ArticlesFragment.newInstance(mTool, mLocale, mCategory), TAG_MAIN_FRAGMENT)
                .commit();
    }

    @Override
    protected void updateToolbarTitle() {
        // try to use the Category Label for the title
        final Manifest manifest = getActiveManifest();
        if (manifest != null) {
            final Optional<String> categoryName = manifest.findCategory(mCategory)
                    .map(Category::getLabel)
                    .map(Text::getText);
            if (categoryName.isPresent()) {
                setTitle(categoryName.get());
                return;
            }
        }

        // otherwise default to the default toolbar title
        super.updateToolbarTitle();
    }
}
