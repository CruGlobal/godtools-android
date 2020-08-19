package org.cru.godtools.base.ui.activity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import org.ccci.gto.android.common.base.Constants.INVALID_LAYOUT_RES

abstract class BaseBindingActivity<B : ViewBinding>(@LayoutRes private val contentLayoutId: Int = INVALID_LAYOUT_RES) :
    BaseActivity() {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
    }

    protected open fun onBindingChanged() = Unit
    // endregion Lifecycle

    // region View & Data Binding
    protected lateinit var binding: B
        private set

    @Suppress("UNCHECKED_CAST")
    protected open fun inflateBinding(): B =
        DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, contentLayoutId, null, false)
            .also { it.lifecycleOwner = this } as B

    private fun setupDataBinding() {
        binding = inflateBinding()
        setContentView(binding.root)
        onBindingChanged()
    }
    // endregion View & Data Binding
}
