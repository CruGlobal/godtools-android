package org.cru.godtools.article.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.cru.godtools.article.R;
import org.cru.godtools.article.fragment.CategoriesFragment;
import org.cru.godtools.base.tool.activity.BaseSingleToolActivity;
import org.cru.godtools.xml.model.Category;

import java.util.Locale;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

public class CategoriesActivity extends BaseSingleToolActivity implements CategoriesFragment.Callbacks {
    private static final String TAG_MAIN_FRAGMENT = "mainFragment";

    public static void start(@NonNull final Activity activity, @NonNull final String toolCode,
                             @NonNull final Locale language) {
        final Bundle extras = new Bundle(2);
        populateExtras(extras, toolCode, language);
        final Intent intent = new Intent(activity, CategoriesActivity.class).putExtras(extras);
        activity.startActivity(intent);
    }

    public CategoriesActivity() {
        super(false);
    }

    // region Lifecycle Events

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFinishing()) {
            return;
        }

        setContentView(R.layout.activity_generic_fragment);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadInitialFragmentIfNeeded();
    }

    @Override
    public void onCategorySelected(@Nullable final Category category) {
        ArticlesActivity.start(this, mTool, mLocale, category != null ? category.getId() : null);
    }

    // endregion Lifecycle Events

    @MainThread
    private void loadInitialFragmentIfNeeded() {
        final FragmentManager fm = getSupportFragmentManager();

        // short-circuit if there is a currently attached fragment
        if (fm.findFragmentByTag(TAG_MAIN_FRAGMENT) != null) {
            return;
        }

        // update the displayed fragment
        fm.beginTransaction()
                .replace(R.id.frame, CategoriesFragment.newInstance(mTool, mLocale), TAG_MAIN_FRAGMENT)
                .commit();
    }
}
