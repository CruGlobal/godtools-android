package org.cru.godtools.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import org.ccci.gto.android.common.recyclerview.adapter.DataBindingViewHolder
import org.cru.godtools.BR
import org.cru.godtools.R

class BannerHeaderAdapter internal constructor(builder: Builder) : BaseEmptyListHeaderFooterAdapter(builder) {
    class Builder : BaseEmptyListHeaderFooterAdapter.Builder<Builder>() {
        fun build() = BannerHeaderAdapter(this)
    }

    var banner: Banner? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed) headerAdapter?.notifyDataSetChanged()
        }

    val callbacks: ObservableField<BannerCallbacks?> = ObservableField()

    override fun getHeaderItemCount() = if (banner != null) 1 else 0
    override fun getHeaderItemViewType(localPosition: Int) = banner?.ordinal ?: 0
    override fun getHeaderItemId(localPosition: Int) = banner?.ordinal?.toLong() ?: NO_ID

    override fun onCreateHeaderItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ViewDataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context), Banner.values()[viewType].layout, parent, false
        )
        binding.setVariable(BR.callbacks, callbacks)
        return DataBindingViewHolder(binding)
    }
}

enum class Banner(@LayoutRes val layout: Int) {
    TUTORIAL_TRAINING(R.layout.banner_tutorial_training)
}

interface BannerCallbacks {
    fun openTrainingTutorial() = Unit
    fun dismissBanner() = Unit
}
