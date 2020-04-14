package org.cru.godtools.adapter

import androidx.databinding.ViewDataBinding
import butterknife.ButterKnife
import org.ccci.gto.android.common.recyclerview.adapter.DataBindingViewHolder

internal abstract class BaseViewHolder<B : ViewDataBinding>(binding: B) : DataBindingViewHolder<B>(binding) {
    init {
        ButterKnife.bind(this, binding.root)
    }

    protected open fun bind(position: Int) = Unit
}
