package org.cru.godtools.tract.activity;

import android.os.Build;
import android.view.View;

import org.cru.godtools.base.ui.activity.BaseActivity;

public abstract class ImmersiveActivity extends BaseActivity {
    /* BEGIN lifecycle */

    @Override
    public void onMultiWindowModeChanged(final boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (!isInMultiWindowMode) {
            makeImmersive();
        } else {
            makeNonImmersive();
        }
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateSystemUi();
        }
    }

    /* END lifecycle */

    private void updateSystemUi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N || !isInMultiWindowMode()) {
            makeImmersive();
        } else {
            makeNonImmersive();
        }
    }

    private void makeImmersive() {
        // enable immersive mode
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                                View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void makeNonImmersive() {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }
}
