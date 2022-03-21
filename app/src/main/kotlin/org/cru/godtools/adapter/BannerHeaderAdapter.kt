package org.cru.godtools.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.headerfooter.AbstractHeaderFooterWrapperAdapter
import com.sergivonavi.materialbanner.Banner
import com.sergivonavi.materialbanner.BannerInterface
import org.cru.godtools.R
import org.cru.godtools.widget.BannerType
import org.cru.godtools.widget.show

class BannerHeaderAdapter : AbstractHeaderFooterWrapperAdapter<BannerViewHolder, RecyclerView.ViewHolder>() {
    var banner: BannerType? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed) headerAdapter?.notifyItemChanged(0)
        }
    var primaryCallback: BannerInterface.OnClickListener? = null
        set(value) {
            field = value
            headerAdapter?.notifyItemChanged(0)
        }
    var secondaryCallback: BannerInterface.OnClickListener? = null
        set(value) {
            field = value
            headerAdapter?.notifyItemChanged(0)
        }

    // region Header
    override fun getHeaderItemCount() = 1
    override fun getHeaderItemId(localPosition: Int) = 1L
    override fun onCreateHeaderItemViewHolder(parent: ViewGroup, viewType: Int) =
        BannerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_banner, parent, false))

    override fun onBindHeaderItemViewHolder(holder: BannerViewHolder, localPosition: Int) {
        holder.bannerView?.show(
            banner = banner,
            primaryCallback = primaryCallback,
            secondaryCallback = secondaryCallback,
            animate = false
        )
    }
    // endregion Header

    // region Footer
    override fun getFooterItemCount() = 0
    override fun onCreateFooterItemViewHolder(parent: ViewGroup, viewType: Int) =
        throw UnsupportedOperationException("onCreateFooterItemViewHolder not supported")
    override fun onBindFooterItemViewHolder(holder: RecyclerView.ViewHolder, localPosition: Int) = Unit
    // endregion Footer
}

class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    internal val bannerView: Banner? = itemView.findViewById(R.id.banner)
}
