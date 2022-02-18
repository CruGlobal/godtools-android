package org.cru.godtools.ui.dashboard.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.databinding.GridConcatViewBinding

class GridConcatAdapter(
    lifecycleOwner: LifecycleOwner,
    private val recyclerAdapter: RecyclerView.Adapter<*>,
    private val rowCount: Int = 1,
    private val titleText: String,
    private val subTitleText: String? = null,
    private val showDivider: Boolean = false
) : SimpleDataBindingAdapter<GridConcatViewBinding>(lifecycleOwner) {
    var hasviews = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    override fun getItemCount() = if (hasviews) {
        1
    } else {
        0
    }
    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        GridConcatViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun onBindViewDataBinding(binding: GridConcatViewBinding, position: Int) {
        binding.titleText = titleText
        binding.subTitleText = subTitleText
        binding.showDivider = showDivider
        binding.concatRecyclerView.adapter = recyclerAdapter
        (binding.concatRecyclerView.layoutManager as? GridLayoutManager)?.spanCount = rowCount
    }
}
