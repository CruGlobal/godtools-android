package org.cru.godtools.base.tool.activity

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import org.cru.godtools.base.ui.activity.BaseActivity

abstract class ImmersiveActivity<B : ViewDataBinding>(
    private val enableImmersive: Boolean,
    @LayoutRes private val contentLayoutId: Int
) : BaseActivity() {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
    }

    protected open fun onBindingChanged() = Unit

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        updateSystemUi()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) updateSystemUi()
    }
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

    private fun updateSystemUi() {
        when {
            // short-circuit for android versions that don't support full-screen/immersive mode
            Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT -> return
            !enableImmersive -> makeNonImmersive()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && isInMultiWindowMode -> makeNonImmersive()
            else -> makeImmersive()
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun makeImmersive() {
        // enable immersive mode
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    private fun makeNonImmersive() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}
