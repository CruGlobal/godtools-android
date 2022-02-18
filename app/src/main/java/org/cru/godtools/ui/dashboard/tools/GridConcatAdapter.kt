package org.cru.godtools.ui.dashboard.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.R

class GridConcatAdapter(
    lifecycleOwner: LifecycleOwner,
    private val recyclerAdapter: RecyclerView.Adapter<*>,
    @LayoutRes private val layoutRes: Int
) : SimpleDataBindingAdapter<ViewDataBinding>(lifecycleOwner), Observer<Boolean> {
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

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int): ViewDataBinding =
        DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutRes, parent, false)

    override fun onBindViewDataBinding(binding: ViewDataBinding, position: Int) {
        binding.root.findViewById<RecyclerView>(R.id.concatRecyclerView).adapter = recyclerAdapter
    }

    override fun onChanged(t: Boolean?) {
        hasviews = t ?: false
    }
}
