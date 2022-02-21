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
    private val dataModel: ToolsCategoryDataModel by viewModels()
    private val toolsDataModel: ToolsAdapterViewModel by viewModels()
    // endregion Data Model

    private fun createCategoryAdapter() = ToolCategoriesAdapter(
        this,
        dataModel.selectedCategory,
        dataModel.primaryLanguage
    ).also { adapter ->
        adapter.callbacks.set(this)
        lifecycle.onDestroy { adapter.callbacks.set(null) }
        dataModel.categories.observe(this, adapter)
    }

    private fun createToolsAdapter() =
        ToolsAdapter(this, toolsDataModel, R.layout.dashboard_list_item_tool).also { adapter ->
            adapter.callbacks.set(this)
            lifecycle.onDestroy { adapter.callbacks.set(null) }
            dataModel.filteredTools.observe(this, adapter)
        }

    private fun createToolsSpotlightAdapter() =
        ToolsAdapter(this, toolsDataModel, R.layout.dashboard_tools_spotlight_list_item).also { adapter ->
            adapter.callbacks.set(this)
            lifecycle.onDestroy { adapter.callbacks.set(null) }
            dataModel.spotlightTools.observe(this, adapter)
        }

    private fun createCombinedAdapter() = ConcatAdapter().also { concatAdapter ->
        concatAdapter.addLayout(R.layout.dashboard_spotlight_concat) {
            it.findViewById<RecyclerView>(R.id.concatRecyclerView).adapter = createToolsSpotlightAdapter()
        }.also { spotlight ->
            dataModel.hasSpotlight.observe(this) { spotlight.repeat = if (it) 1 else 0 }
        }
        concatAdapter.addLayout(R.layout.dashboard_tools_ui_categories) {
            it.findViewById<RecyclerView>(R.id.concatRecyclerView).adapter = createCategoryAdapter()
        }.also { category ->
            dataModel.hasCategories.observe(this) {
                category.repeat = if (it) 1 else 0
            }
        }
        concatAdapter.addAdapter(createToolsAdapter())
    }

    override fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) {
        super.onSyncData(helper, force)
        helper.sync(syncService.syncTools(force))
    }

    // region Lifecycle
    override fun onBindingCreated(binding: DashboardToolsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.refresh.setupSwipeRefresh()
        binding.mainRecyclerView.adapter = createCombinedAdapter()
    }

    override fun onDestroyBinding(binding: DashboardToolsFragmentBinding) {
        binding.mainRecyclerView.adapter = null
    }
    //endregion Lifecycle

    override fun onCategorySelected(category: String?) {
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
