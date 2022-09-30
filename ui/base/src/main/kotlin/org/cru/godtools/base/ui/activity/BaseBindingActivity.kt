package org.cru.godtools.base.ui.activity

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import org.ccci.gto.android.common.base.Constants

abstract class BaseBindingActivity<B : ViewBinding> protected constructor(@LayoutRes private val contentLayoutId: Int) :
    BaseActivity() {
    protected constructor() : this(Constants.INVALID_LAYOUT_RES)

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
    }

    protected open fun onBindingChanged() = Unit

    @CallSuper
    override fun onContentChanged() {
        super.onContentChanged()
        setupActionBar()
    }

    @CallSuper
    protected open fun onSetupActionBar() = Unit
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

    // region ActionBar
    protected open val toolbar: Toolbar? get() = null

    private fun setupActionBar() {
        toolbar?.let { setSupportActionBar(it) }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // trigger lifecycle event for subclasses
        onSetupActionBar()
    }
    // endregion ActionBar
}
