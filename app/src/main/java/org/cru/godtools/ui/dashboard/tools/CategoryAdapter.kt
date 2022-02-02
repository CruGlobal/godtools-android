package org.cru.godtools.ui.dashboard.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import org.cru.godtools.databinding.DashboardListItemCategoryBinding
import org.cru.godtools.tool.model.Category

class CategoryAdapter() : RecyclerView.Adapter<CategoryAdapter.ViewHolder>(), Observer<List<String>> {

    private var categories: List<String> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = categories.size

    override fun onChanged(t: List<String>) {
        categories = t
    }

    class ViewHolder(val binding: DashboardListItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        DashboardListItemCategoryBinding
            .inflate(LayoutInflater.from(parent.context))
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.category = categories[position]
    }


}

