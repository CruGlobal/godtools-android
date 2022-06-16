package org.cru.godtools.ui.dashboard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import dagger.hilt.android.AndroidEntryPoint
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.R
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.databinding.DashboardHomeFragmentBinding
import org.cru.godtools.ui.dashboard.DashboardActivity
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks

@AndroidEntryPoint
class HomeFragment : BaseFragment<DashboardHomeFragmentBinding>(R.layout.dashboard_home_fragment) {
    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        DashboardHomeFragmentBinding.inflate(inflater, container, false)

    @CallSuper
    override fun onBindingCreated(binding: DashboardHomeFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.frame.setContent {
            HomeLayout(
                onOpenTool = { tool, tr1, tr2 -> findListener<ToolsAdapterCallbacks>()?.openTool(tool, tr1, tr2) },
                onOpenToolDetails = { findListener<ToolsAdapterCallbacks>()?.showToolDetails(it) },
                onViewAllTools = { findListener<DashboardActivity>()?.showPage(Page.ALL_TOOLS) }
            )
        }
    }
}
