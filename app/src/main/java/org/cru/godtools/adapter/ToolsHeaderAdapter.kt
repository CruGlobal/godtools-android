package org.cru.godtools.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.ccci.gto.android.common.recyclerview.adapter.DataBindingViewHolder
import org.cru.godtools.content.ToolHeader
import org.cru.godtools.databinding.ToolsTutorialBannerBinding
import org.cru.godtools.fragment.HeaderBannerCallbacks

class ToolsHeaderAdapter internal constructor(builder: Builder) : BaseEmptyListHeaderFooterAdapter(builder) {
    class Builder : BaseEmptyListHeaderFooterAdapter.Builder<Builder>() {
        fun build(): ToolsHeaderAdapter {
            return ToolsHeaderAdapter(this).apply {
                this.adapterToolHeader = toolHeader
            }
        }

        fun toolHeader(header: ToolHeader): Builder {
            this.toolHeader = header
            return this
        }

        lateinit var toolHeader: ToolHeader
    }

    // region Header

    private lateinit var adapterToolHeader: ToolHeader

    var isHeaderVisible: Boolean = true
        set(value) {
            field = value
            headerAdapter?.notifyDataSetChanged()
        }

    lateinit var headerBannerCallbacks: HeaderBannerCallbacks

    override fun getHeaderItemCount(): Int {
        return if (isHeaderVisible) 1 else 0
    }

    override fun onBindHeaderItemViewHolder(holder: RecyclerView.ViewHolder, localPosition: Int) =
        (holder as DataBindingViewHolder<ToolsTutorialBannerBinding>).let {
            it.binding.callback = headerBannerCallbacks
            it.binding.toolsDismissText.setText(adapterToolHeader.dismissText)
            it.binding.bannerOpenDescriptionText.setText(adapterToolHeader.descriptionText)
            it.binding.bannerOpenText.setText(adapterToolHeader.openText)
        }

    override fun onCreateHeaderItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return DataBindingViewHolder<ToolsTutorialBannerBinding>(
            ToolsTutorialBannerBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }
    // endregion Header
}
