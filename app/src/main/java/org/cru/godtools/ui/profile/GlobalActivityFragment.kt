package org.cru.godtools.ui.profile

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.AndroidSupportInjection
import org.ccci.gto.android.common.db.findLiveData
import org.cru.godtools.R
import org.cru.godtools.databinding.ProfilePageGlobalActivityFragmentBinding
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.sync.syncGlobalActivity
import org.keynote.godtools.android.db.GodToolsDao
import javax.inject.Inject

class GlobalActivityFragment :
    BasePlatformFragment<ProfilePageGlobalActivityFragmentBinding>(R.layout.profile_page_global_activity_fragment) {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: GlobalActivityFragmentViewModel by viewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onBindingCreated(binding: ProfilePageGlobalActivityFragmentBinding, savedInstanceState: Bundle?) {
        binding.globalActivity = viewModel.globalActivity
    }

    override fun syncData(force: Boolean) {
        super.syncData(force)
        syncHelper.sync(requireContext().syncGlobalActivity(force))
    }
}

class GlobalActivityFragmentViewModel @Inject constructor(dao: GodToolsDao) : ViewModel() {
    val globalActivity = dao.findLiveData<GlobalActivityAnalytics>(1)
}
