package org.cru.godtools.article.adapter

import android.view.LayoutInflater
import android.view.ViewGroup

import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.article.databinding.ListItemCategoryBinding
import org.cru.godtools.xml.model.Category

class CategoriesAdapter : SimpleDataBindingAdapter<ListItemCategoryBinding>() {
    var callbacks: Callbacks? = null
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount)
        }

    var categories: List<Category>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    interface Callbacks {
        fun onCategorySelected(category: Category?)
    }

    override fun getItemCount(): Int {
        return categories?.size ?: 0
    }

    // region Lifecycle Events

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int): ListItemCategoryBinding {
        return ListItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    }

    override fun onBindViewDataBinding(binding: ListItemCategoryBinding, position: Int) {
        binding.callbacks = callbacks
        binding.category = categories?.get(position)
    }

    override fun onViewDataBindingRecycled(binding: ListItemCategoryBinding) {
        super.onViewDataBindingRecycled(binding)
        binding.callbacks = null
        binding.category = null
    }

    // endregion Lifecycle Events
}
