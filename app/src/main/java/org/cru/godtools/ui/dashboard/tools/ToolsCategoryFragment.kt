package org.cru.godtools.ui.dashboard.tools

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.R
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.databinding.DashboardToolsCategoryFragmentBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks
import org.cru.godtools.ui.tools.ToolsAdapterViewModel
import org.cru.godtools.ui.tools.ToolsListFragment
import javax.inject.Inject

@AndroidEntryPoint
class ToolsCategoryFragment() :
    BaseFragment<DashboardToolsCategoryFragmentBinding>(R.layout.dashboard_tools_category_fragment),
    CategoryAdapterCallbacks, ToolsAdapterCallbacks {

    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager

    @Inject
    internal lateinit var settings: Settings

    // region Data Model
    private val dataModel: ToolsCategoryDataModel by viewModels()
    private val toolsDataModel: ToolsAdapterViewModel by viewModels()

    // endregion Data Model

    private val categoryAdapter: CategoryAdapter by lazy {
        CategoryAdapter(this as CategoryAdapterCallbacks).also { adapter ->
            dataModel.categories.observe(this, adapter)
        }
    }

    private val toolsAdapter: ToolsCategoryAdapter by lazy {
        ToolsCategoryAdapter(this, toolsDataModel).also { adapter ->
            dataModel.viewTools.observe(this, adapter)
        }
    }

    override fun onBindingCreated(binding: DashboardToolsCategoryFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.categoryRecyclerView.adapter = categoryAdapter
        binding.toolsRecyclerView.adapter = toolsAdapter
    }

    override fun onCategorySelected(category: String) {
        val selectedCategory: String? = if (categoryAdapter.selectedCategory == category) {
            null
        } else {
            category
        }
        dataModel.selectedCategory.value = selectedCategory
        categoryAdapter.selectedCategory = selectedCategory
    }

    // region ToolsAdapterCallbacks
    override fun openTool(tool: Tool?, primary: Translation?, parallel: Translation?) {}

    override fun addTool(code: String?) {
        code?.let { downloadManager.pinToolAsync(it) }
    }

    override fun removeTool(tool: Tool?, translation: Translation?) {
        tool?.code?.let { downloadManager.unpinToolAsync(it) }

    }

    override fun onToolInfo(code: String?) {
        findListener<ToolsListFragment.Callbacks>()?.onToolInfo(code)
    }

    override fun onToolsReordered(vararg ids: Long) {}
    // endregion ToolsAdapterCallbacks
}
