package org.cru.godtools.ui.dashboard.tools

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.onDestroy
import org.cru.godtools.R
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.databinding.DashboardToolsCategoryFragmentBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tooldetails.startToolDetailsActivity
import org.cru.godtools.ui.tools.ToolsAdapter
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks
import org.cru.godtools.ui.tools.ToolsAdapterViewModel

@AndroidEntryPoint
class ToolsCategoryFragment :
    BaseFragment<DashboardToolsCategoryFragmentBinding>(R.layout.dashboard_tools_category_fragment),
    CategoryAdapterCallbacks,
    ToolsAdapterCallbacks {

    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager

    @Inject
    internal lateinit var settings: Settings

    // region Data Model
    private val dataModel: ToolsCategoryDataModel by viewModels()
    private val toolsDataModel: ToolsAdapterViewModel by viewModels()

    // endregion Data Model

    private val categoryAdapter: CategoryAdapter by lazy {
        CategoryAdapter(this, dataModel.selectedCategory).also { adapter ->
            adapter.callbacks.set(this)
            lifecycle.onDestroy { adapter.callbacks.set(null) }
            dataModel.categories.observe(this, adapter)
        }
    }

    private val toolsAdapter: ToolsAdapter by lazy {
        ToolsAdapter(this, toolsDataModel, R.layout.dashboard_list_item_tools).also { adapter ->
            adapter.callbacks.set(this)
            lifecycle.onDestroy { adapter.callbacks.set(null) }
            dataModel.filteredTools.observe(this, adapter)
        }
    }

    // region lifecycle
    override fun onBindingCreated(binding: DashboardToolsCategoryFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.categoryRecyclerView.adapter = categoryAdapter
        binding.toolsRecyclerView.adapter = toolsAdapter
    }

    override fun onDestroyBinding(binding: DashboardToolsCategoryFragmentBinding) {
        binding.categoryRecyclerView.adapter = null
        binding.toolsRecyclerView.adapter = null
    }
    //endregion lifecycle

    override fun onCategorySelected(category: String) {
        with(dataModel.selectedCategory) { value = if (value != category) category else null }
    }

    // region ToolsAdapterCallbacks
    override fun addTool(code: String?) {
        code?.let { downloadManager.pinToolAsync(it) }
    }

    override fun removeTool(tool: Tool?, translation: Translation?) {
        tool?.code?.let { downloadManager.unpinToolAsync(it) }
    }

    override fun onToolInfo(code: String?) {
        code?.let { requireActivity().startToolDetailsActivity(code) }
    }

    override fun onToolsReordered(vararg ids: Long) = Unit
    override fun openTool(tool: Tool?, primary: Translation?, parallel: Translation?) = Unit
    // endregion ToolsAdapterCallbacks
}
