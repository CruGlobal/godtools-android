package org.cru.godtools.ui.dashboard.tools

import android.os.Bundle
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ConcatAdapter.Config
import androidx.recyclerview.widget.ConcatAdapter.Config.StableIdMode.ISOLATED_STABLE_IDS
import androidx.recyclerview.widget.RecyclerView
import com.sergivonavi.materialbanner.BannerInterface
import dagger.hilt.android.AndroidEntryPoint
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.ccci.gto.android.common.androidx.recyclerview.widget.addLayout
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.cru.godtools.R
import org.cru.godtools.adapter.BannerAdapter
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_ALL_TOOLS
import org.cru.godtools.analytics.firebase.model.FirebaseIamActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_ALL_TOOLS
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.DashboardToolsFragmentBinding
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tools.ToolViewModels
import org.cru.godtools.ui.tools.ToolsAdapter
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks
import org.cru.godtools.widget.BannerType

@AndroidEntryPoint
class ToolsFragment :
    BasePlatformFragment<DashboardToolsFragmentBinding>(R.layout.dashboard_tools_fragment),
    ToolCategoriesAdapter.Callbacks,
    ToolsAdapterCallbacks {
    interface Callbacks : ToolsAdapterCallbacks

    // region Data Model
    private val dataModel: ToolsFragmentDataModel by viewModels()
    private val toolViewModels: ToolViewModels by viewModels()
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

    override fun onResume() {
        super.onResume()
        eventBus.post(AnalyticsScreenEvent(SCREEN_ALL_TOOLS))
        eventBus.post(FirebaseIamActionEvent(ACTION_IAM_ALL_TOOLS))
    }
    //endregion Lifecycle

    // region UI
    private fun RecyclerView.setupRecyclerView() {
        adapter = ConcatAdapter(Config.Builder().setStableIdMode(ISOLATED_STABLE_IDS).build()).apply {
            // Banner Adapter
            addAdapter(
                BannerAdapter().also {
                    it.primaryCallback = BannerInterface.OnClickListener {
                        when (dataModel.banner.value) {
                            BannerType.TOOL_LIST_FAVORITES ->
                                settings.setFeatureDiscovered(Settings.FEATURE_TOOL_FAVORITE)
                            else -> Unit
                        }
                    }
                    dataModel.banner.observe(viewLifecycleOwner, it)
                }
            )

            // Tool Spotlight adapter
            addLayout(R.layout.dashboard_tools_spotlight, 0) {
                it.findViewById<ComposeView>(R.id.compose)?.setContent {
                    GodToolsTheme {
                        ToolSpotlight(
                            onOpenToolDetails = { findListener<Callbacks>()?.showToolDetails(it) },
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }.apply {
                dataModel.spotlightTools.asLiveData()
                    .observe(viewLifecycleOwner) { repeat = if (it.isNotEmpty()) 1 else 0 }
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
                ToolsAdapter(viewLifecycleOwner, toolViewModels, R.layout.dashboard_list_item_tool).also {
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
    override fun onToolClicked(tool: Tool?, primary: Translation?, parallel: Translation?) = showToolDetails(tool?.code)

    override fun openTool(tool: Tool?, primary: Translation?, parallel: Translation?) {
        findListener<Callbacks>()?.openTool(tool, primary, parallel)
    }

    override fun showToolDetails(code: String?) {
        findListener<Callbacks>()?.showToolDetails(code)
    }

    override fun pinTool(code: String?) {
        findListener<Callbacks>()?.pinTool(code)
    }

    override fun unpinTool(tool: Tool?) {
        findListener<Callbacks>()?.unpinTool(tool)
    }
    // endregion ToolsAdapterCallbacks
}
