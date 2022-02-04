package org.cru.godtools.ui.dashboard.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import org.cru.godtools.base.Settings
import org.cru.godtools.databinding.DashboardListItemToolsBinding
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks
import org.cru.godtools.ui.tools.ToolsAdapterViewModel

class ToolsCategoryAdapter(
    val callbacks: ToolsAdapterCallbacks,
    val dataModel: ToolsAdapterViewModel
) : RecyclerView.Adapter<ToolsCategoryAdapter.ViewHolder>(), Observer<List<Tool>> {

    private var tools: List<Tool> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = tools.size
    override fun onChanged(t: List<Tool>?) {
        tools = t ?: emptyList()
    }

    class ViewHolder(val binding: DashboardListItemToolsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        DashboardListItemToolsBinding.inflate(LayoutInflater.from(parent.context)).also {

        }
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tool = tools[position]
        holder.binding.tool = tool
        val toolViewModel = dataModel.ToolViewModel(tool.code ?: "")
        holder.binding.parallelLanguage = toolViewModel.parallelLanguage
        holder.binding.parallelTranslation = toolViewModel.parallelTranslation
        holder.binding.primaryTranslation = toolViewModel.firstTranslation
        holder.binding.banner = toolViewModel.banner

    }
}
