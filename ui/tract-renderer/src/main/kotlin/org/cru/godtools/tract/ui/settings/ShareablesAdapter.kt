package org.cru.godtools.tract.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.tool.model.shareable.Shareable
import org.cru.godtools.tool.model.shareable.ShareableImage
import org.cru.godtools.tool.tract.BR
import org.cru.godtools.tool.tract.R
import org.cru.godtools.tract.databinding.TractSettingsSheetCallbacks

class ShareablesAdapter(lifecycleOwner: LifecycleOwner, private val callbacks: TractSettingsSheetCallbacks) :
    SimpleDataBindingAdapter<ViewDataBinding>(lifecycleOwner) {
    var shareables: List<Shareable>? = null
        set(value) {
            field = value?.filterIsInstance<ShareableImage>()
            notifyDataSetChanged()
        }

    override fun getItemCount() = shareables?.size ?: 0
    private fun getItem(position: Int) = shareables?.getOrNull(position)
    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ShareableImage -> R.layout.tract_settings_item_shareable_image
        else -> R.layout.tract_settings_item_shareable_image
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context), viewType, parent, false)

    override fun onViewDataBindingCreated(binding: ViewDataBinding, viewType: Int) {
        super.onViewDataBindingCreated(binding, viewType)
        binding.setVariable(BR.callbacks, callbacks)
    }

    override fun onBindViewDataBinding(binding: ViewDataBinding, position: Int) {
        binding.setVariable(BR.item, getItem(position))
    }
}
