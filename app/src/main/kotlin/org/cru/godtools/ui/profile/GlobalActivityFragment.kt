package org.cru.godtools.ui.profile

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.ccci.gto.android.common.db.findLiveData
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.cru.godtools.R
import org.cru.godtools.databinding.ProfilePageGlobalActivityFragmentBinding
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.GlobalActivityAnalytics
import org.keynote.godtools.android.db.GodToolsDao

@AndroidEntryPoint
class GlobalActivityFragment :
    BasePlatformFragment<ProfilePageGlobalActivityFragmentBinding>(R.layout.profile_page_global_activity_fragment) {
    private val viewModel: GlobalActivityFragmentViewModel by viewModels()

    override fun onBindingCreated(binding: ProfilePageGlobalActivityFragmentBinding, savedInstanceState: Bundle?) {
        binding.globalActivity = viewModel.globalActivity
    }

    override fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) {
        super.onSyncData(helper, force)
        helper.sync(syncService.syncGlobalActivity(force))
    }
}

@HiltViewModel
class GlobalActivityFragmentViewModel @Inject constructor(dao: GodToolsDao) : ViewModel() {
    val globalActivity = dao.findLiveData<GlobalActivityAnalytics>(1)
}
