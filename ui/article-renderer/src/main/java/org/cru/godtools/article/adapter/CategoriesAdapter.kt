package org.cru.godtools.article.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.article.databinding.ListItemCategoryBinding
import org.cru.godtools.xml.model.Category

class CategoriesAdapter : SimpleDataBindingAdapter<ListItemCategoryBinding>() {
    interface Callbacks {
        fun onCategorySelected(category: Category?)
    }

    private val callbacks = ObservableField<Callbacks>()
    var categories: List<Category>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return categories?.size ?: 0
    }

    fun setCallbacks(callbacks: Callbacks?) = this.callbacks.set(callbacks)

    // region Lifecycle Events

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int): ListItemCategoryBinding {
        return ListItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .also { it.callbacks = callbacks }
    }

    override fun onBindViewDataBinding(binding: ListItemCategoryBinding, position: Int) {
        binding.category = categories?.get(position)
    }

    override fun onViewDataBindingRecycled(binding: ListItemCategoryBinding) {
        super.onViewDataBindingRecycled(binding)
        binding.category = null
    }

    // endregion Lifecycle Events
}
