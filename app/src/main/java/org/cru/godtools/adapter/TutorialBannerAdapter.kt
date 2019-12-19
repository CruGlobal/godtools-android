package org.cru.godtools.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.ccci.gto.android.common.recyclerview.adapter.DataBindingViewHolder
import org.cru.godtools.databinding.ToolsTutorialBannerBinding
import org.cru.godtools.fragment.TrainingBannerCallbacks

class TutorialBannerAdapter : RecyclerView.Adapter<TutorialBannerAdapter.ViewHolder>() {
    var listener: TrainingBannerCallbacks? = null
    var isTutorialVisible: Boolean? = true
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class ViewHolder(binding: ToolsTutorialBannerBinding) : DataBindingViewHolder<ToolsTutorialBannerBinding>(binding)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ToolsTutorialBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return if (isTutorialVisible == true) 1 else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.callback = listener
    }
}
