package org.cru.godtools.tract.activity;

import android.view.View;

import org.cru.godtools.base.ui.activity.BaseActivity;

class ImmersiveActivity extends BaseActivity {
    /* BEGIN lifecycle */

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        makeImmersive();
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeImmersive();
    }

    /* END lifecycle */

    private void makeImmersive() {
        //TODO: enable immersive mode
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
