package org.cru.godtools.ui.profile

import android.os.Bundle
import androidx.fragment.app.viewModels
import org.cru.godtools.R
import org.cru.godtools.databinding.FragmentGlobalDashboardBinding
import org.cru.godtools.fragment.BaseBindingPlatformFragment
import org.cru.godtools.sync.syncGlobalAnalytics
import org.cru.godtools.viewmodel.GlobalDashboardDataModel
import java.util.Calendar

class GlobalDashboardFragment : BaseBindingPlatformFragment<FragmentGlobalDashboardBinding>(R.layout.fragment_global_dashboard) {
    private val viewModel: GlobalDashboardDataModel by viewModels()

    override fun onBindingCreated(binding: FragmentGlobalDashboardBinding, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
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
