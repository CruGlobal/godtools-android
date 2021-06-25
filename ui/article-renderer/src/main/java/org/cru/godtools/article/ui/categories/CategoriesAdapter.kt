package org.cru.godtools.article.ui.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView.NO_ID
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.ccci.gto.android.common.support.v4.util.IdUtils
import org.cru.godtools.article.databinding.ListItemCategoryBinding
import org.cru.godtools.tool.model.Category

internal class CategoriesAdapter(lifecycleOwner: LifecycleOwner? = null) :
    SimpleDataBindingAdapter<ListItemCategoryBinding>(lifecycleOwner), Observer<List<Category>?> {
    init {
        setHasStableIds(true)
    }

    val callbacks = ObservableField<CategorySelectedListener>()
    var categories: List<Category>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = categories?.size ?: 0
    private fun getItem(position: Int) = categories?.get(position)
    override fun getItemId(position: Int) = getItem(position)?.id?.let { IdUtils.convertId(it) } ?: NO_ID

    // region Lifecycle
    override fun onChanged(t: List<Category>?) {
        categories = t
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        ListItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .also { it.callbacks = callbacks }

    override fun onBindViewDataBinding(binding: ListItemCategoryBinding, position: Int) {
        binding.category = getItem(position)
    }

    override fun onViewDataBindingRecycled(binding: ListItemCategoryBinding) {
        super.onViewDataBindingRecycled(binding)
        binding.category = null
    }
    // endregion Lifecycle
}
