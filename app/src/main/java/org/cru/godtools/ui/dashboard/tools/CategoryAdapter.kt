package org.cru.godtools.ui.dashboard.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.databinding.DashboardListItemCategoryBinding

class CategoryAdapter(lifecycleOwner: LifecycleOwner, private val dataModel: ToolsCategoryDataModel) :
    SimpleDataBindingAdapter<DashboardListItemCategoryBinding>(lifecycleOwner), Observer<List<String>> {

    val callbacks = ObservableField<CategoryAdapterCallbacks>()

    private var categories: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = categories.size

    override fun onChanged(t: List<String>) {
        categories = t
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) = DashboardListItemCategoryBinding
        .inflate(LayoutInflater.from(parent.context), parent, false).also {
            it.callbacks = callbacks
        }

    override fun onBindViewDataBinding(binding: DashboardListItemCategoryBinding, position: Int) {
        val category = categories[position]
        binding.category = category
        binding.selectedCategory = dataModel.selectedCategory
    }
}

interface CategoryAdapterCallbacks {
    fun onCategorySelected(category: String)
}
