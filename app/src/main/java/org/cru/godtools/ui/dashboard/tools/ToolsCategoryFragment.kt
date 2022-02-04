package org.cru.godtools.ui.dashboard.tools

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
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
import org.cru.godtools.ui.tools.ToolsAdapterViewModel

@AndroidEntryPoint
class ToolsCategoryFragment() :
    BaseFragment<DashboardToolsCategoryFragmentBinding>(R.layout.dashboard_tools_category_fragment),
    CategoryAdapterCallbacks,
    ToolsCategoryAdapter.Callbacks {

    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager

    @Inject
    internal lateinit var settings: Settings

    // region Data Model
    private val dataModel: ToolsCategoryDataModel by viewModels()
    private val toolsDataModel: ToolsAdapterViewModel by viewModels()

    // endregion Data Model

    private val categoryAdapter: CategoryAdapter by lazy {
        CategoryAdapter().also { adapter ->
            adapter.callbacks.set(this)
            lifecycle.onDestroy { adapter.callbacks.set(null) }
            dataModel.categories.observe(this, adapter)
        }
    }

    private val toolsAdapter: ToolsCategoryAdapter by lazy {
        ToolsCategoryAdapter(ViewModelProvider(this), viewLifecycleOwner).also { adapter ->
            adapter.callbacks.set(this)
            lifecycle.onDestroy { adapter.callbacks.set(null) }
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

    // region ToolsCategoryAdapterCallbacks
    override fun addTool(code: String?) {
        code?.let { downloadManager.pinToolAsync(it) }
    }

    override fun removeTool(tool: Tool?, translation: Translation?) {
        tool?.code?.let { downloadManager.unpinToolAsync(it) }
    }

    override fun onToolInfo(code: String?) {
        code?.let { requireActivity().startToolDetailsActivity(code) }
    }
    // endregion ToolsCategoryAdapterCallbacks
}
