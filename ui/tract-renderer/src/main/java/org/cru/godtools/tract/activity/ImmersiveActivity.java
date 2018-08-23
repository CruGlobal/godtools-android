package org.cru.godtools.tract.activity;

import android.os.Build;
import android.view.View;

import org.cru.godtools.base.ui.activity.BaseActivity;

public abstract class ImmersiveActivity extends BaseActivity {
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !isInMultiWindowMode()) {
            makeImmersive();
        } else {
            makeNonImmersive();
        }
    }

    private void makeImmersive() {
        // Only enable Full-screen/Immersive starting in KitKat
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

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
