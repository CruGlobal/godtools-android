package org.keynote.godtools.renderer.crureader;

import android.app.Application;

import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

/**
 * Created by rmatt on 11/16/2016.
 */
public class RenderApp extends Application {

    private RenderSingleton renderSingleton;

    @Override
    public void onCreate() {
        super.onCreate();
        this.renderSingleton = RenderSingleton.init(this);
    }
}
