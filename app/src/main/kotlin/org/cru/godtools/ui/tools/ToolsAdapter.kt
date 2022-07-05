package org.cru.godtools.ui.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView.NO_ID
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.BR
import org.cru.godtools.model.Tool

open class ToolsAdapter(
    lifecycleOwner: LifecycleOwner,
    private val toolViewModels: ToolViewModels,
    @LayoutRes private val itemLayout: Int
) : SimpleDataBindingAdapter<ViewDataBinding>(lifecycleOwner), Observer<List<Tool>> {
    init {
        setHasStableIds(true)
    }

    val callbacks = ObservableField<ToolsAdapterCallbacks>()
    private var tools: List<Tool>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = tools?.size ?: 0
    private fun getItem(position: Int) = tools?.get(position)
    override fun getItemId(position: Int) = getItem(position)?.id ?: NO_ID

    // region Lifecycle
    override fun onChanged(t: List<Tool>?) {
        tools = t
    }

    override fun getItemViewType(position: Int) = itemLayout
    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), viewType, parent, false)
            .also { it.setVariable(BR.callbacks, callbacks) }

    override fun onBindViewDataBinding(binding: ViewDataBinding, position: Int) {
        binding.setVariable(BR.toolViewModel, getItem(position)?.code?.let { toolViewModels[it] })
    }
    // endregion Lifecycle
}
