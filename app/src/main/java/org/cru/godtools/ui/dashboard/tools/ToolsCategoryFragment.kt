package org.cru.godtools.ui.dashboard.tools

import android.os.Bundle
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.R
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.databinding.DashboardToolsCategoryFragmentBinding

@AndroidEntryPoint
class ToolsCategoryFragment() :
    BaseFragment<DashboardToolsCategoryFragmentBinding>(R.layout.dashboard_tools_category_fragment) {

    // region Data Model
    private val dataModel: ToolsCategoryDataModel by viewModels()

    // endregion Data Model

    private val adapter: CategoryAdapter by lazy {
        CategoryAdapter().also { adapter ->
            dataModel.categories.observe(this, adapter)
        }
    }

    override fun onBindingCreated(binding: DashboardToolsCategoryFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.categoryRecyclerView.adapter = adapter
    }
}
