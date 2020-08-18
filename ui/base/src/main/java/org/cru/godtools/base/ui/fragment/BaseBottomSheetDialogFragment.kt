package org.cru.godtools.base.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import org.ccci.gto.android.common.dagger.viewmodel.DaggerSavedStateViewModelProviderFactory

abstract class BaseBottomSheetDialogFragment<B : ViewBinding> : BottomSheetDialogFragment() {
    // region Lifecycle
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDataBinding(view, savedInstanceState)
    }

    override fun onDestroyView() {
        cleanupDataBinding()
        super.onDestroyView()
    }
    // endregion Lifecycle

    // region ViewModelProvider.Factory
    @Inject
    internal lateinit var viewModelProviderFactory: DaggerSavedStateViewModelProviderFactory
    private val defaultViewModelProvider by lazy { viewModelProviderFactory.create(this, arguments) }

    override fun getDefaultViewModelProviderFactory() = defaultViewModelProvider
    // endregion ViewModelProvider.Factory

    // region View & Data Binding
    protected var binding: B? = null
    @Suppress("UNCHECKED_CAST")
    protected open val View.viewBinding get() = DataBindingUtil.bind<ViewDataBinding>(this) as? B

    private fun setupDataBinding(view: View, savedInstanceState: Bundle?) {
        binding = view.viewBinding?.also {
            if (it is ViewDataBinding) it.lifecycleOwner = viewLifecycleOwner
            onBindingCreated(it, savedInstanceState)
        }
    }

    private fun cleanupDataBinding() {
        binding?.let { onDestroyBinding(it) }
        binding = null
    }

    open fun onBindingCreated(binding: B, savedInstanceState: Bundle?) = Unit
    open fun onDestroyBinding(binding: B) = Unit
    // endregion View & Data Binding
}
