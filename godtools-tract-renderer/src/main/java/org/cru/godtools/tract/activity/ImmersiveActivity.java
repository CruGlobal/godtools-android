package org.cru.godtools.tract.activity;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import butterknife.ButterKnife;

class ImmersiveActivity extends AppCompatActivity implements LifecycleRegistryOwner {
    private final LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    /* BEGIN lifecycle */

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
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

    @Override
    public LifecycleRegistry getLifecycle() {
        return mLifecycleRegistry;
    }
}
