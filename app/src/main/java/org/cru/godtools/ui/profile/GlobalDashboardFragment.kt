package org.cru.godtools.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import org.cru.godtools.R
import org.cru.godtools.databinding.FragmentGlobalDashboardBinding
import org.cru.godtools.fragment.BaseBindingPlatformFragment
import org.cru.godtools.viewmodel.GlobalDashboardDataModel
import java.util.Calendar

class GlobalDashboardFragment : BaseBindingPlatformFragment<FragmentGlobalDashboardBinding>(R.layout.fragment_global_dashboard) {
    private val viewModel: GlobalDashboardDataModel by viewModels()

    override fun onBindingCreated(binding: FragmentGlobalDashboardBinding, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.year = "${Calendar.getInstance().get(Calendar.YEAR)}"
    }
}
