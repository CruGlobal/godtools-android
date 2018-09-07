package org.cru.godtools.article.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter;
import org.cru.godtools.article.databinding.ListItemCategoryBinding;
import org.cru.godtools.xml.model.Category;

import java.util.List;

public class CategoriesAdapter extends SimpleDataBindingAdapter<ListItemCategoryBinding> {
    public interface Callbacks {
        void onCategorySelected(@Nullable final Category category);
    }

    @Nullable
    private List<Category> mCategories;

    @Nullable
    private Callbacks mCallbacks;

    @Override
    public int getItemCount() {
        return mCategories != null ? mCategories.size() : 0;
    }

    public void setCategories(@Nullable final List<Category> categories) {
        mCategories = categories;
        notifyDataSetChanged();
    }

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
        notifyItemRangeChanged(0, getItemCount());
    }

    // region Lifecycle Events

    @NonNull
    @Override
    protected ListItemCategoryBinding onCreateViewDataBinding(@NonNull final ViewGroup parent, final int viewType) {
        return ListItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }

    @Override
    protected void onBindViewDataBinding(@NonNull final ListItemCategoryBinding binding, final int position) {
        assert mCategories != null : "mCategories has to be defined to trigger onBindViewDataBinding";
        binding.setCallbacks(mCallbacks);
        binding.setCategory(mCategories.get(position));
    }

    @Override
    protected void onViewDataBindingRecycled(@NonNull final ListItemCategoryBinding binding) {
        super.onViewDataBindingRecycled(binding);
        binding.setCallbacks(null);
        binding.setCategory(null);
    }

    // endregion Lifecycle Events
}
