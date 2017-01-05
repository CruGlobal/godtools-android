package org.keynote.godtools.android;


import org.keynote.godtools.android.snuffy.SnuffyApplication;

public class DebugGodToolsApplication extends SnuffyApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //Stetho.initializeWithDefaults(this);
    }
}
