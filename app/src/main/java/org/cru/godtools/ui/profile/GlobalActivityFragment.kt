package org.cru.godtools.ui.profile

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import org.cru.godtools.R
import org.cru.godtools.databinding.FragmentGlobalDashboardBinding
import org.cru.godtools.fragment.BaseBindingPlatformFragment
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.sync.syncGlobalAnalytics
import org.keynote.godtools.android.db.GodToolsDao
import java.text.NumberFormat
import java.util.Calendar

class GlobalActivityFragment :
    BaseBindingPlatformFragment<FragmentGlobalDashboardBinding>(R.layout.fragment_global_dashboard) {
    private val viewModel: GlobalActivityFragmentViewModel by viewModels()

    override fun onBindingCreated(binding: FragmentGlobalDashboardBinding, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
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

class GlobalActivityFragmentViewModel(app: Application) : AndroidViewModel(app) {
    private val globalActivityAnalytics =
        GodToolsDao.getInstance(getApplication()).findLiveData(GlobalActivityAnalytics::class.java, 1)

    val uniqueUsers = globalActivityAnalytics.map { it?.users.formatNumber() }
    val gospelPresentation = globalActivityAnalytics.map { it?.gospelPresentation.formatNumber() }
    val sessions = globalActivityAnalytics.map { it?.launches.formatNumber() }
    val countries = globalActivityAnalytics.map { it?.countries.formatNumber() }

    private fun Int?.formatNumber(): String {
        return NumberFormat.getInstance().format(this ?: 0)
    }
}
