package org.cru.godtools.ui.dashboard.tools

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.R
import org.cru.godtools.databinding.DashboardToolsFragmentBinding
import org.cru.godtools.fragment.BasePlatformFragment

@AndroidEntryPoint
class ToolsFragment : BasePlatformFragment<DashboardToolsFragmentBinding>(R.layout.dashboard_tools_fragment) {

    private val dataModel: ToolsFragmentDataModel by viewModels()
    override fun onBindingCreated(binding: DashboardToolsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.refresh.setupSwipeRefresh()
        binding.hasSpotlight = dataModel.hasSpotlight
    }
}
