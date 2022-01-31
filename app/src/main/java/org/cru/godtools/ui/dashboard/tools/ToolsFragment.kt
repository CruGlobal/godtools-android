package org.cru.godtools.ui.dashboard.tools

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.R
import org.cru.godtools.databinding.ToolsContainerFragmentBinding
import org.cru.godtools.fragment.BasePlatformFragment

@AndroidEntryPoint
class ToolsFragment : BasePlatformFragment<ToolsContainerFragmentBinding>(R.layout.tools_container_fragment) {

    private val dataModel: ToolsFragmentDataModel by viewModels()
    override fun onBindingCreated(binding: ToolsContainerFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.refresh.setupSwipeRefresh()
        binding.hasSpotlight = dataModel.hasSpotlight
    }
}
