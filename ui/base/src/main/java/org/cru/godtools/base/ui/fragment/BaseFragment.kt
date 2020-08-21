package org.cru.godtools.base.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject
import org.ccci.gto.android.common.base.Constants.INVALID_LAYOUT_RES
import org.ccci.gto.android.common.dagger.viewmodel.DaggerSavedStateViewModelProviderFactory

abstract class BaseFragment<B : ViewBinding> @JvmOverloads constructor(
    @LayoutRes contentLayoutId: Int? = INVALID_LAYOUT_RES
) : Fragment(contentLayoutId ?: INVALID_LAYOUT_RES) {
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
    private var binding: B? = null
    protected open val hasDataBinding get() = true
    @Suppress("UNCHECKED_CAST")
    protected open val View.viewBinding: B?
        get() = if (hasDataBinding) DataBindingUtil.bind<ViewDataBinding>(this) as? B else null

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
