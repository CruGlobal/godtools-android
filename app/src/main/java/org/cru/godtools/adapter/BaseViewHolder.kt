package org.cru.godtools.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife

internal abstract class BaseViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
    init {
        ButterKnife.bind(this, rootView)
    }

    protected open fun bind(position: Int) = Unit
}
