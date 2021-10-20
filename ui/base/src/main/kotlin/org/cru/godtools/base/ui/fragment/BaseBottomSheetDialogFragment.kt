package org.cru.godtools.base.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment<B : ViewBinding> : BottomSheetDialogFragment() {
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
