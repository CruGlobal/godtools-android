package org.cru.godtools.ui.profile

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import org.ccci.gto.android.common.db.findLiveData
import org.ccci.gto.android.common.sync.swiperefreshlayout.widget.SwipeRefreshSyncHelper
import org.cru.godtools.R
import org.cru.godtools.databinding.ProfilePageGlobalActivityFragmentBinding
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.sync.syncGlobalActivity
import org.keynote.godtools.android.db.GodToolsDao
import javax.inject.Inject

class GlobalActivityFragment :
    BasePlatformFragment<ProfilePageGlobalActivityFragmentBinding>(R.layout.profile_page_global_activity_fragment) {
    private val viewModel: GlobalActivityFragmentViewModel by viewModels()

    override fun onBindingCreated(binding: ProfilePageGlobalActivityFragmentBinding, savedInstanceState: Bundle?) {
        binding.globalActivity = viewModel.globalActivity
    }

    override fun onSyncData(helper: SwipeRefreshSyncHelper, force: Boolean) {
        super.onSyncData(helper, force)
        helper.sync(requireContext().syncGlobalActivity(force))
    }
}

class GlobalActivityFragmentViewModel @Inject constructor(dao: GodToolsDao) : ViewModel() {
    val globalActivity = dao.findLiveData<GlobalActivityAnalytics>(1)
}
