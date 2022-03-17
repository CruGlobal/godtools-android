package org.cru.godtools.base.tool.ui.share

import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.base.tool.BR
import org.cru.godtools.base.tool.R

internal class ShareAppsAdapter(private val items: List<ResolveInfo>, private val callbacks: Callbacks) :
    SimpleDataBindingAdapter<ViewDataBinding>() {
    interface Callbacks {
        fun onOpenApp(info: ResolveInfo)
        fun onShowChooser()
    }

    override fun getItemCount() = items.size + 1
    override fun getItemViewType(position: Int) = when (position) {
        items.size -> R.layout.tool_share_item_more
        else -> R.layout.tool_share_item_app
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), viewType, parent, false).also {
            it.setVariable(BR.callbacks, callbacks)
        }

    override fun onBindViewDataBinding(binding: ViewDataBinding, position: Int) {
        binding.setVariable(BR.info, items.getOrNull(position))
    }

    override fun onViewDataBindingRecycled(binding: ViewDataBinding) {
        binding.setVariable(BR.info, null)
    }
}
