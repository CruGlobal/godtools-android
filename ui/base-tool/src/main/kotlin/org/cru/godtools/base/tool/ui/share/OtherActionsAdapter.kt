package org.cru.godtools.base.tool.ui.share

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.base.tool.ui.share.model.ShareItem
import org.cru.godtools.tool.BR

class OtherActionsAdapter(
    lifecycleOwner: LifecycleOwner,
    items: List<ShareItem>,
    private val callbacks: Callbacks? = null
) : SimpleDataBindingAdapter<ViewDataBinding>(lifecycleOwner) {
    interface Callbacks {
        fun triggerAction(item: ShareItem?)
    }

    private val items = items.filter { it.actionLayout != null }

    override fun getItemCount() = items.size
    override fun getItemViewType(position: Int) = items[position].actionLayout!!

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int): ViewDataBinding =
        DataBindingUtil.inflate<ViewDataBinding?>(LayoutInflater.from(parent.context), viewType, parent, false).also {
            it.setVariable(BR.callbacks, callbacks)
        }

    override fun onBindViewDataBinding(binding: ViewDataBinding, position: Int) {
        binding.setVariable(BR.item, items[position])
    }
}
