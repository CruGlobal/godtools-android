package org.cru.godtools.base.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder

abstract class BaseFragment<B : ViewDataBinding> @JvmOverloads constructor(@LayoutRes layoutId: Int? = null) :
    Fragment(layoutId ?: 0) {
    // region Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDataBinding(view, savedInstanceState)
        bindButterKnife(view)
    }

    override fun onDestroyView() {
        unbindButterKnife()
        cleanupDataBinding()
        super.onDestroyView()
    }
    // endregion Lifecycle

    // region ButterKnife
    private var butterKnife: Unbinder? = null

    private fun bindButterKnife(view: View) {
        butterKnife = ButterKnife.bind(this, view)
    }

    private fun unbindButterKnife() {
        butterKnife?.unbind()
        butterKnife = null
    }
    // endregion ButterKnife

    // region Data Binding
    private var binding: B? = null
    protected open val hasDataBinding get() = true

    private fun setupDataBinding(view: View, savedInstanceState: Bundle?) {
        if (hasDataBinding) {
            binding = DataBindingUtil.bind(view)
            binding?.let {
                it.lifecycleOwner = viewLifecycleOwner
                onBindingCreated(it, savedInstanceState)
            }
        }
    }

    private fun cleanupDataBinding() {
        binding?.let { onDestroyBinding(it) }
        binding = null
    }

    open fun onBindingCreated(binding: B, savedInstanceState: Bundle?) = Unit
    open fun onDestroyBinding(binding: B) = Unit
    // endregion Data Binding
}
