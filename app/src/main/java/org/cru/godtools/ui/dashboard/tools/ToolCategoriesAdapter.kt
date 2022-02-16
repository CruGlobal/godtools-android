package org.cru.godtools.ui.dashboard.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.Locale
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.databinding.DashboardListItemCategoryBinding

class ToolCategoriesAdapter(
    lifecycleOwner: LifecycleOwner,
    private val selectedCategory: LiveData<String?>,
    private val primaryLanguage: LiveData<Locale>
) : SimpleDataBindingAdapter<DashboardListItemCategoryBinding>(lifecycleOwner), Observer<List<String>> {
    val callbacks = ObservableField<CategoryAdapterCallbacks>()
    private var categories = emptyList<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = categories.size

    override fun onChanged(t: List<String>) {
        categories = t
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        DashboardListItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false).also {
            it.callbacks = callbacks
            it.selectedCategory = selectedCategory
            it.primaryLanguage = primaryLanguage
        }

    override fun onBindViewDataBinding(binding: DashboardListItemCategoryBinding, position: Int) {
        binding.category = categories[position]
    }
}

interface CategoryAdapterCallbacks {
    fun onCategorySelected(category: String?)
}
