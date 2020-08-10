package org.cru.godtools.base.tool.activity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import org.cru.godtools.base.ui.activity.BaseActivity

abstract class ImmersiveActivity<B : ViewDataBinding>(@LayoutRes private val contentLayoutId: Int) : BaseActivity() {
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
