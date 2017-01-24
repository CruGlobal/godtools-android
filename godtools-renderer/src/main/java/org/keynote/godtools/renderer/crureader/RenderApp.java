package org.keynote.godtools.renderer.crureader;

import android.app.Application;

import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

public abstract class RenderApp extends Application {

    private RenderSingleton renderSingleton;

    @Override
    public void onCreate() {
        super.onCreate();
        this.renderSingleton = RenderSingleton.init(this);
        RenderSingleton.getInstance().setBaseAppConfig(getBaseAppConfig());
    }

    public abstract BaseAppConfig getBaseAppConfig();
}
