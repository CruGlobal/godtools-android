package org.cru.godtools.ui.dashboard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.R
import org.cru.godtools.base.ui.dashboard.Page
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.ComposeLayoutBinding
import org.cru.godtools.ui.dashboard.DashboardActivity
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class HomeFragment : BaseFragment<ComposeLayoutBinding>(R.layout.compose_layout) {
    @Inject
    internal lateinit var eventBus: EventBus

    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeLayoutBinding.inflate(inflater, container, false)

    @CallSuper
    override fun onBindingCreated(binding: ComposeLayoutBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.compose.setContent {
            GodToolsTheme {
                HomeLayout(
                    onOpenTool = { tool, tr1, tr2 -> findListener<ToolsAdapterCallbacks>()?.openTool(tool, tr1, tr2) },
                    onOpenToolDetails = { findListener<ToolsAdapterCallbacks>()?.showToolDetails(it) },
                    onViewAllFavorites = { findListener<DashboardActivity>()?.showPage(Page.FAVORITE_TOOLS) },
                    onViewAllTools = { findListener<DashboardActivity>()?.showPage(Page.ALL_TOOLS) }
                )
            }
        }
    }
}
