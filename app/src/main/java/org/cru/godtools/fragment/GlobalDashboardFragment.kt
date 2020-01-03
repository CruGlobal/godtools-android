package org.cru.godtools.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.cru.godtools.databinding.FragmentGlobalDashboardBinding

class GlobalDashboardFragment : BasePlatformFragment() {

    private lateinit var binding: FragmentGlobalDashboardBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGlobalDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
}
