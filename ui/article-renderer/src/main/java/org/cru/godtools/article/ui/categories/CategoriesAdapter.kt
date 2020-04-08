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
import org.cru.godtools.xml.model.Category

class CategoriesAdapter(lifecycleOwner: LifecycleOwner? = null) :
    SimpleDataBindingAdapter<ListItemCategoryBinding>(lifecycleOwner), Observer<List<Category>?> {
    interface Callbacks {
        fun onCategorySelected(category: Category?)
    }

    init {
        setHasStableIds(true)
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

    override fun getItemId(position: Int): Long {
        return categories?.get(position)?.id?.let { IdUtils.convertId(it) } ?: NO_ID
    }

    fun setCallbacks(callbacks: Callbacks?) = this.callbacks.set(callbacks)

    // region Lifecycle Events
    override fun onChanged(t: List<Category>?) {
        categories = t
    }

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
