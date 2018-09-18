package org.cru.godtools.article.fragment;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ccci.gto.android.common.recyclerview.decorator.VerticalSpaceItemDecoration;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.cru.godtools.article.R;
import org.cru.godtools.article.R2;
import org.cru.godtools.article.adapter.CategoriesAdapter;
import org.cru.godtools.base.tool.fragment.BaseToolFragment;
import org.cru.godtools.xml.model.Category;

import java.util.Locale;

import butterknife.BindView;

public class CategoriesFragment extends BaseToolFragment implements CategoriesAdapter.Callbacks {
    public interface Callbacks {
        void onCategorySelected(@Nullable Category category);
    }

    @Nullable
    @BindView(R2.id.categories)
    RecyclerView mCategoriesView;
    @Nullable
    CategoriesAdapter mCategoriesAdapter;

    public static Fragment newInstance(@NonNull final String code, @NonNull final Locale locale) {
        final CategoriesFragment fragment = new CategoriesFragment();
        final Bundle args = new Bundle(2);
        populateArgs(args, code, locale);
        fragment.setArguments(args);
        return fragment;
    }

    // region Lifecycle Events

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupCategoriesView();
    }

    @Override
    @CallSuper
    protected void onManifestUpdated() {
        super.onManifestUpdated();
        updateCategoriesView();
    }

    @Override
    public void onCategorySelected(@Nullable final Category category) {
        final Callbacks callbacks = FragmentUtils.getListener(this, Callbacks.class);
        if (callbacks != null) {
            callbacks.onCategorySelected(category);
        }
    }

    @Override
    public void onDestroyView() {
        cleanupCategoriesView();
        super.onDestroyView();
    }

    // endregion Lifecycle Events

    // region Categories View

    private void setupCategoriesView() {
        if (mCategoriesView != null) {
            mCategoriesView.setHasFixedSize(true);
            mCategoriesView.addItemDecoration(new VerticalSpaceItemDecoration(R.dimen.categories_list_gap));

            mCategoriesAdapter = new CategoriesAdapter();
            mCategoriesAdapter.setCallbacks(this);
            mCategoriesView.setAdapter(mCategoriesAdapter);
            updateCategoriesView();
        }
    }

    private void updateCategoriesView() {
        if (mCategoriesAdapter != null) {
            mCategoriesAdapter.setCategories(mManifest != null ? mManifest.getCategories() : null);
        }
    }

    private void cleanupCategoriesView() {
        if (mCategoriesAdapter != null) {
            mCategoriesAdapter.setCallbacks(null);
        }
        mCategoriesAdapter = null;
    }

    // endregion Categories View
}
