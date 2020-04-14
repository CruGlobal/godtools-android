package org.cru.godtools.adapter

import android.database.Cursor
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import org.ccci.gto.android.common.recyclerview.adapter.CursorAdapter
import org.ccci.gto.android.common.recyclerview.adapter.DataBindingViewHolder

@Deprecated("This is a temporary class to help migrate ToolsAdapter to utilize data binding")
abstract class CursorDataBindingAdapter<B : ViewDataBinding, VH : DataBindingViewHolder<B>>(
    private val lifecycleOwner: LifecycleOwner? = null
) : CursorAdapter<VH>() {
    // region Lifecycle
    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        onCreateViewDataBinding(parent, viewType)
            .also { it.lifecycleOwner = lifecycleOwner }
            .let { onCreateViewHolder(it, viewType) }

    @CallSuper
    override fun onBindViewHolder(holder: VH, cursor: Cursor?, position: Int) {
        onBindViewDataBinding(holder.binding, cursor, position)
        holder.binding.executePendingBindings()
    }

    protected abstract fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int): B
    protected abstract fun onCreateViewHolder(binding: B, viewType: Int): VH
    protected abstract fun onBindViewDataBinding(binding: B, cursor: Cursor?, position: Int)
    override fun onViewRecycled(holder: VH) = onViewDataBindingRecycled(holder.binding)
    protected open fun onViewDataBindingRecycled(binding: B) = Unit
    // end Lifecycle
}
