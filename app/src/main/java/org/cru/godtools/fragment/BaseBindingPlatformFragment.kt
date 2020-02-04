package org.cru.godtools.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseBindingPlatformFragment<T : ViewDataBinding>(@LayoutRes private val layoutId: Int) :
    BasePlatformFragment() {
    private var binding: T? = null

    // region Lifecycle
    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        DataBindingUtil.inflate<T>(inflater, layoutId, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { onBindingCreated(it, savedInstanceState) }
    }

    open fun onBindingCreated(binding: T, savedInstanceState: Bundle?) = Unit

    override fun onDestroyView() {
        binding?.let { onDestroyBinding(it) }
        binding = null
        super.onDestroyView()
    }

    open fun onDestroyBinding(binding: T) = Unit
    // endregion Lifecycle
}
