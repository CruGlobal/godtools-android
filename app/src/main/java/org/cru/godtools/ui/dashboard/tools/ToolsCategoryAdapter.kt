package org.cru.godtools.ui.dashboard.tools

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.databinding.DashboardListItemToolsBinding
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tools.ToolsAdapterViewModel

class ToolsCategoryAdapter(viewModelProvider: ViewModelProvider, lifecycleOwner: LifecycleOwner) :
    SimpleDataBindingAdapter<DashboardListItemToolsBinding>(lifecycleOwner), Observer<List<Tool>> {

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

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        DashboardListItemToolsBinding.inflate(LayoutInflater.from(parent.context)).also {
            it.callbacks = callbacks.get()
            it.lifecycleOwner = lifecycleOwner
        }

    override fun onBindViewDataBinding(binding: DashboardListItemToolsBinding, position: Int) {
        val tool = tools[position]
        binding.tool = tool

        tool.code?.let {
            binding.parallelLanguage = dataModel.getToolViewModel(it).parallelLanguage
            binding.parallelTranslation = dataModel.getToolViewModel(it).parallelTranslation
            binding.primaryTranslation = dataModel.getToolViewModel(it).firstTranslation
            binding.downloadProgress = dataModel.getToolViewModel(it).downloadProgress
            binding.banner = dataModel.getToolViewModel(it).banner
        }
    }
}
