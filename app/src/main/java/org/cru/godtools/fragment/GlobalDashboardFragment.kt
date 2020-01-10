package org.cru.godtools.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.map
import org.cru.godtools.databinding.FragmentGlobalDashboardBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.GlobalActivityAnalytics

class GlobalDashboardFragment : BasePlatformFragment() {

    private var binding: FragmentGlobalDashboardBinding? = null

    private lateinit var viewModel: GlobalDashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GlobalDashboardViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGlobalDashboardBinding.inflate(inflater, container, false).also {
            it.viewModel = viewModel
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getGlobalActivityData(requireContext())

        viewModel.users.observe(this, Observer { users ->
            users
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

class GlobalDashboardViewModel : ViewModel() {

    val globalActivity by lazy { MutableLiveData<GlobalActivityAnalytics>() }

    val users by lazy { globalActivity.map { it?.users.toString() } }
    val sessions by lazy {  globalActivity.map { it?.launches.toString() } }
    val countries by lazy {  globalActivity.map { it?.countries.toString() } }
    val presentations by lazy {  globalActivity.map { it?.gospelPresentation.toString() } }

    fun getGlobalActivityData(context: Context) {
        val manager = GodToolsDownloadManager.getInstance(context)
        globalActivity.value = manager.globalActivity
    }
}
