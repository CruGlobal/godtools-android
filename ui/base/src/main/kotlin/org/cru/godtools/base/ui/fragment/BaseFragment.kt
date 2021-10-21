package org.cru.godtools.base.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B : ViewBinding> protected constructor(@LayoutRes contentLayoutId: Int) :
    Fragment(contentLayoutId) {
    // region Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDataBinding(view, savedInstanceState)
    }

    override fun onDestroyView() {
        cleanupDataBinding()
        super.onDestroyView()
    }
    // endregion Lifecycle

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
