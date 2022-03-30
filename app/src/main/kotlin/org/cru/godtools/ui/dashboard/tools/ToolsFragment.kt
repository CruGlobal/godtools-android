package org.cru.godtools.ui.dashboard.tools

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ConcatAdapter.Config
import androidx.recyclerview.widget.ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
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
    private val dataModel: ToolsFragmentDataModel by viewModels()
    private val toolsDataModel: ToolsAdapterViewModel by viewModels()
    // endregion Data Model

    // region Lifecycle
    override fun onBindingCreated(binding: DashboardToolsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.refresh.setupSwipeRefresh()
        binding.recyclerView.setupRecyclerView()
    }

    override fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) {
        super.onSyncData(helper, force)
        helper.sync(syncService.syncTools(force))
    }
    //endregion Lifecycle

    // region UI
    private fun RecyclerView.setupRecyclerView() {
        adapter = ConcatAdapter(Config.Builder().setStableIdMode(ISOLATED_STABLE_IDS).build()).apply {
            // Tool Spotlight adapter
            val spotlightAdapter =
                ToolsAdapter(viewLifecycleOwner, toolsDataModel, R.layout.dashboard_tools_spotlight_tool).also {
                    dataModel.spotlightTools.observe(viewLifecycleOwner, it)
                    it.callbacks.set(this@ToolsFragment)
                }
            addLayout(R.layout.dashboard_tools_spotlight, 0) {
                it.findViewById<RecyclerView>(R.id.tools)?.adapter = spotlightAdapter
            }.apply {
                dataModel.spotlightTools.observe(viewLifecycleOwner) { repeat = if (it.isNotEmpty()) 1 else 0 }
            }

            // Tool Categories
            val categoriesAdapter =
                ToolCategoriesAdapter(viewLifecycleOwner, dataModel.selectedCategory, settings.primaryLanguageLiveData)
                    .also {
                        dataModel.categories.observe(viewLifecycleOwner, it)
                        it.callbacks.set(this@ToolsFragment)
                    }
            addLayout(R.layout.dashboard_tools_categories, 0) {
                it.findViewById<RecyclerView>(R.id.categories)?.adapter = categoriesAdapter
            }.apply { dataModel.categories.observe(viewLifecycleOwner) { repeat = if (it.isNotEmpty()) 1 else 0 } }

            // Tools
            addAdapter(
                ToolsAdapter(viewLifecycleOwner, toolsDataModel, R.layout.dashboard_list_item_tool).also {
                    dataModel.filteredTools.observe(viewLifecycleOwner, it)
                    it.callbacks.set(this@ToolsFragment)
                }
            )
        }
    }
    // endregion UI

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