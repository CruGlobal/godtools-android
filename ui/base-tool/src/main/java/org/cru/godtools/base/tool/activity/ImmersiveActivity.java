package org.cru.godtools.base.tool.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

import org.cru.godtools.base.ui.activity.BaseActivity;

public abstract class ImmersiveActivity extends BaseActivity {
    private final boolean mEnableImmersive;

    public ImmersiveActivity(final boolean immersive) {
        mEnableImmersive = immersive;
    }

    // region Lifecycle Events

    @Override
    public void onMultiWindowModeChanged(final boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        updateSystemUi();
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateSystemUi();
        }
    }

    // endregion Lifecycle Events

    private void updateSystemUi() {
        // short-circuit for android versions before KitKat because they don't support full-screen/immersive mode
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        // toggle immersive/non-immersive mode based on state
        if (!mEnableImmersive) {
            // Force non-immersive if immersive is not enabled
            makeNonImmersive();
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            makeImmersive();
        } else if (!isInMultiWindowMode()) {
            makeImmersive();
        } else {
            makeNonImmersive();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void makeImmersive() {
        // enable immersive mode
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                                View.SYSTEM_UI_FLAG_FULLSCREEN |
                                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                                View.SYSTEM_UI_FLAG_IMMERSIVE |
                                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void makeNonImmersive() {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }
}
