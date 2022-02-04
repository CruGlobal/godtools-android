package org.cru.godtools.ui.dashboard.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import org.cru.godtools.databinding.DashboardListItemToolsBinding
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tools.ToolsAdapterViewModel

class ToolsCategoryAdapter(
    viewModelProvider: ViewModelProvider
) : RecyclerView.Adapter<ToolsCategoryAdapter.ViewHolder>(), Observer<List<Tool>> {

    interface Callbacks {
        fun onToolInfo(code: String?)
        fun addTool(code: String?)
        fun removeTool(tool: Tool?, translation: Translation?)
    }
    val callbacks = ObservableField<Callbacks>()
    private val dataModel = viewModelProvider.get(ToolsAdapterViewModel::class.java)

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
            it.callbacks = callbacks.get()
        }
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tool = tools[position]
        holder.binding.tool = tool

        tool.code?.let {
            holder.binding.parallelLanguage = dataModel.getToolViewModel(it).parallelLanguage
            holder.binding.parallelTranslation = dataModel.getToolViewModel(it).parallelTranslation
            holder.binding.primaryTranslation = dataModel.getToolViewModel(it).firstTranslation
            holder.binding.downloadProgress = dataModel.getToolViewModel(it).downloadProgress
            holder.binding.banner = dataModel.getToolViewModel(it).banner
        }
    }
}
