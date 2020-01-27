package org.cru.godtools.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.thekey.android.TheKey
import org.cru.godtools.databinding.FragmentGlobalDashboardBinding
import org.cru.godtools.viewmodel.GlobalDashboardDataModel

class GlobalDashboardFragment : BasePlatformFragment() {
    private var binding: FragmentGlobalDashboardBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGlobalDashboardBinding.inflate(inflater, container, false)
        binding?.viewModel = GlobalDashboardDataModel()
        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding?.viewModel?.theKey?.value = TheKey.getInstance(requireContext())
    }
}
