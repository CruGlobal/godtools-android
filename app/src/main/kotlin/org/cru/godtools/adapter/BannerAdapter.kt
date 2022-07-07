package org.cru.godtools.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.sergivonavi.materialbanner.Banner
import com.sergivonavi.materialbanner.BannerInterface
import org.cru.godtools.R
import org.cru.godtools.widget.BannerType
import org.cru.godtools.widget.show

class BannerAdapter : RecyclerView.Adapter<BannerViewHolder>(), Observer<BannerType?> {
    init {
        setHasStableIds(true)
    }

    var banner: BannerType? = null
        set(value) {
            val changed = field != value
            field = value
            if (changed) notifyItemChanged(0)
        }
    var primaryCallback: BannerInterface.OnClickListener? = null
        set(value) {
            field = value
            notifyItemChanged(0)
        }
    var secondaryCallback: BannerInterface.OnClickListener? = null
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onChanged(t: BannerType?) {
        banner = t
    }

    override fun getItemCount() = 1
    override fun getItemId(localPosition: Int) = 1L
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BannerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.widget_banner, parent, false))

    override fun onBindViewHolder(holder: BannerViewHolder, localPosition: Int) {
        holder.bannerView?.show(
            banner = banner,
            primaryCallback = primaryCallback,
            secondaryCallback = secondaryCallback,
            animate = false
        )
    }
}

class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    internal val bannerView: Banner? = itemView.findViewById(R.id.banner)
}
