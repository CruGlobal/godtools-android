package org.cru.godtools.ui.profile

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import org.ccci.gto.android.common.db.findLiveData
import org.cru.godtools.R
import org.cru.godtools.databinding.FragmentGlobalDashboardBinding
import org.cru.godtools.fragment.BaseBindingPlatformFragment
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.sync.syncGlobalAnalytics
import org.keynote.godtools.android.db.GodToolsDao
import java.util.Calendar

class GlobalActivityFragment :
    BaseBindingPlatformFragment<FragmentGlobalDashboardBinding>(R.layout.fragment_global_dashboard) {
    private val viewModel: GlobalActivityFragmentViewModel by viewModels()

    override fun onBindingCreated(binding: FragmentGlobalDashboardBinding, savedInstanceState: Bundle?) {
        binding.globalActivity = viewModel.globalActivity
        binding.year = "${Calendar.getInstance().get(Calendar.YEAR)}"
    }

    override fun onResume() {
        super.onResume()
        syncData(false)
    }

    override fun syncData(force: Boolean) {
        super.syncData(force)
        syncHelper.sync(syncGlobalAnalytics(requireContext(), force))
    }
}

class GlobalActivityFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = GodToolsDao.getInstance(application)
    val globalActivity = dao.findLiveData<GlobalActivityAnalytics>(1)
}
