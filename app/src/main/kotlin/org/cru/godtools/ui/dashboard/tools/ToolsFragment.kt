package org.cru.godtools.ui.dashboard.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.R
import org.cru.godtools.analytics.firebase.model.ACTION_IAM_ALL_TOOLS
import org.cru.godtools.analytics.firebase.model.FirebaseIamActionEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_ALL_TOOLS
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.ComposeLayoutBinding
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks
import org.greenrobot.eventbus.EventBus

@AndroidEntryPoint
class ToolsFragment : BaseFragment<ComposeLayoutBinding>(R.layout.compose_layout) {
    @Inject
    internal lateinit var eventBus: EventBus

    // region Lifecycle
    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeLayoutBinding.inflate(inflater, container, false)

    @CallSuper
    override fun onBindingCreated(binding: ComposeLayoutBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.compose.setContent {
            GodToolsTheme {
                ToolsLayout(onToolClicked = { findListener<ToolsAdapterCallbacks>()?.showToolDetails(it) })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        eventBus.post(AnalyticsScreenEvent(SCREEN_ALL_TOOLS))
        eventBus.post(FirebaseIamActionEvent(ACTION_IAM_ALL_TOOLS))
    }
    //endregion Lifecycle
}
