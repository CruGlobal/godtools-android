package org.cru.godtools.ui.dashboard.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.Locale
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.databinding.DashboardToolsCategoriesCategoryBinding

class ToolCategoriesAdapter(
    lifecycleOwner: LifecycleOwner,
    private val selectedCategory: LiveData<String?>,
    private val primaryLanguage: LiveData<Locale>
) : SimpleDataBindingAdapter<DashboardToolsCategoriesCategoryBinding>(lifecycleOwner), Observer<List<String>> {
    interface Callbacks {
        fun onCategorySelected(category: String?)
    }

    val callbacks = ObservableField<Callbacks>()
    private var categories = emptyList<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = categories.size + 1

    override fun onChanged(t: List<String>) {
        categories = t
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        DashboardToolsCategoriesCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false).also {
            it.callbacks = callbacks
            it.selectedCategory = selectedCategory
            it.primaryLanguage = primaryLanguage
        }

    override fun onBindViewDataBinding(binding: DashboardToolsCategoriesCategoryBinding, position: Int) {
        binding.category = categories.getOrNull(position - 1)
    }
}
