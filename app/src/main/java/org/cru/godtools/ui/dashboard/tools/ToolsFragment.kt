package org.cru.godtools.ui.dashboard.tools

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.onDestroy
import org.ccci.gto.android.common.androidx.recyclerview.widget.addLayout
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.cru.godtools.R
import org.cru.godtools.databinding.DashboardToolsFragmentBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tooldetails.startToolDetailsActivity
import org.cru.godtools.ui.tools.ToolsAdapter
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks
import org.cru.godtools.ui.tools.ToolsAdapterViewModel

@AndroidEntryPoint
class ToolsFragment :
    BasePlatformFragment<DashboardToolsFragmentBinding>(R.layout.dashboard_tools_fragment),
    ToolCategoriesAdapter.Callbacks,
    ToolsAdapterCallbacks {
    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager

    // region Data Model
    private val toolsCategoryDataModel: ToolsCategoryDataModel by viewModels()
    private val toolsDataModel: ToolsAdapterViewModel by viewModels()
    // endregion Data Model

    private val categoryAdapter: ToolCategoriesAdapter by lazy {
        ToolCategoriesAdapter(
            this,
            toolsCategoryDataModel.selectedCategory,
            toolsCategoryDataModel.primaryLanguage
        ).also { adapter ->
            adapter.callbacks.set(this)
            lifecycle.onDestroy { adapter.callbacks.set(null) }
            toolsCategoryDataModel.categories.observe(this, adapter)
        }
    }

    private val toolsCategoryAdapter: ToolsAdapter by lazy {
        ToolsAdapter(this, toolsDataModel, R.layout.dashboard_list_item_tool).also { adapter ->
            adapter.callbacks.set(this)
            lifecycle.onDestroy { adapter.callbacks.set(null) }
            toolsCategoryDataModel.filteredTools.observe(this, adapter)
        }
    }

    private val toolsSpotlightAdapter: ToolsAdapter by lazy {
        ToolsAdapter(this, toolsDataModel, R.layout.dashboard_tools_spotlight_list_item).also { adapter ->
            adapter.callbacks.set(this)
            lifecycle.onDestroy { adapter.callbacks.set(null) }
            toolsCategoryDataModel.spotlightTools.observe(this, adapter)
        }
    }

    private val combinedToolsAdapter: ConcatAdapter by lazy {
        ConcatAdapter().also { concatAdapter ->
            concatAdapter.addLayout(R.layout.dashboard_spotlight_concat) {
                it.findViewById<RecyclerView>(R.id.concatRecyclerView).adapter = toolsSpotlightAdapter
            }
            concatAdapter.addLayout(R.layout.dashboard_tools_ui_categories) {
                it.findViewById<RecyclerView>(R.id.concatRecyclerView).adapter = categoryAdapter
            }
            concatAdapter.addAdapter(toolsCategoryAdapter)
        }
    }

    override fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) {
        super.onSyncData(helper, force)
        helper.sync(syncService.syncTools(force))
    }

    // region Lifecycle
    override fun onBindingCreated(binding: DashboardToolsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.refresh.setupSwipeRefresh()
        binding.mainRecyclerView.adapter = combinedToolsAdapter
    }

    override fun onDestroyBinding(binding: DashboardToolsFragmentBinding) {
        binding.mainRecyclerView.adapter = null
    }
    //endregion Lifecycle

    override fun onCategorySelected(category: String?) {
        with(toolsCategoryDataModel.selectedCategory) { value = if (value != category) category else null }
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
