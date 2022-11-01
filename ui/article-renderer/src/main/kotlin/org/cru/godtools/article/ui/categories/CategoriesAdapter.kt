package org.cru.godtools.article.ui.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView.NO_ID
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.ccci.gto.android.common.util.Ids
import org.cru.godtools.shared.tool.parser.model.Category
import org.cru.godtools.tool.article.databinding.ListItemCategoryBinding

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
    override fun getItemId(position: Int) = getItem(position)?.id?.let { Ids.generate(it) } ?: NO_ID

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
