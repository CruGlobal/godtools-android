package org.cru.godtools.base.ui.activity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseBindingActivity<B : ViewDataBinding>(@LayoutRes private val contentLayoutId: Int) : BaseActivity() {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
    }

    protected open fun onBindingChanged() = Unit
    // endregion Lifecycle

    // region DataBinding
    protected lateinit var binding: B
        private set

    private fun setupDataBinding() {
        binding = DataBindingUtil.inflate(layoutInflater, contentLayoutId, null, false)!!
        binding.lifecycleOwner = this
        setContentView(binding.root)
        onBindingChanged()
    }
    // endregion DataBinding
}
