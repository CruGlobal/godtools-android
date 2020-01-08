package org.cru.godtools.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import org.cru.godtools.config.BuildConfig.MOBILE_CONTENT_API
import org.cru.godtools.databinding.FragmentGlobalDashboardBinding
import org.cru.godtools.global.activity.analytics.manger.service.GlobalActivityAnalyticsManager

class GlobalDashboardFragment : BasePlatformFragment() {

    private var binding: FragmentGlobalDashboardBinding? = null

    private lateinit var viewModel: GlobalDashboardViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GlobalDashboardViewModel::class.java).also {
            it.context = requireContext()
            it.dataManger = GlobalActivityAnalyticsManager(requireContext(), MOBILE_CONTENT_API)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGlobalDashboardBinding.inflate(inflater, container, false).also {
            it.viewModel = viewModel
        }
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

class GlobalDashboardViewModel : ViewModel() {

    var context: Context? = null

    var dataManger: GlobalActivityAnalyticsManager? = null

    val globalActivityAnalytics = dataManger?.getGlobalActivityLiveData()
}
