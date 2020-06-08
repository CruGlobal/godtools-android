package org.cru.godtools.base.tool.activity

import android.annotation.TargetApi
import android.os.Build
import android.view.View
import androidx.annotation.LayoutRes
import org.cru.godtools.base.ui.activity.BaseActivity

abstract class ImmersiveActivity(private val enableImmersive: Boolean, @LayoutRes contentLayoutId: Int) :
    BaseActivity(contentLayoutId) {
    // region Lifecycle
    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        updateSystemUi()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) updateSystemUi()
    }
    // endregion Lifecycle

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
