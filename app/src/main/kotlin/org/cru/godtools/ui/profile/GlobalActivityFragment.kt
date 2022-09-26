package org.cru.godtools.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.ProfilePageGlobalActivityFragmentBinding
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.ui.account.globalactivity.AccountGlobalActivityLayout

@AndroidEntryPoint
class GlobalActivityFragment :
    BasePlatformFragment<ProfilePageGlobalActivityFragmentBinding>(R.layout.profile_page_global_activity_fragment) {
    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ProfilePageGlobalActivityFragmentBinding.inflate(inflater, container, false).apply {
        compose.setContent {
            GodToolsTheme {
                AccountGlobalActivityLayout()
            }
        }
    }

    override fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) {
        super.onSyncData(helper, force)
        helper.sync(syncService.syncGlobalActivity(force))
    }
}
