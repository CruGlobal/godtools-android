package org.cru.godtools.ui.dashboard.tools

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.R
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.databinding.DashboardToolsSpotlightFragmentBinding
import org.cru.godtools.ui.tools.ToolsAdapter
import org.cru.godtools.ui.tools.ToolsAdapterViewModel

@AndroidEntryPoint
class ToolsSpotlightFragment :
    BaseFragment<DashboardToolsSpotlightFragmentBinding>(R.layout.dashboard_tools_spotlight_fragment) {

    private val spotlightDataModel: ToolsFragmentDataModel by viewModels()
    private val dataModel: ToolsAdapterViewModel by viewModels()
    private val adapter: ToolsAdapter by lazy {
        ToolsAdapter(this, dataModel).also {
            // TODO: add observer for Spotlight Tools
        }
    }

    override fun onBindingCreated(binding: DashboardToolsSpotlightFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.lifecycleOwner = this
        binding.spotlightRecyclerView.adapter = adapter
    }

    override fun onDestroyBinding(binding: DashboardToolsSpotlightFragmentBinding) {
        binding.spotlightRecyclerView.adapter = null
    }
}
