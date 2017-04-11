package org.keynote.godtools.android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.facebook.stetho.Stetho;

import org.keynote.godtools.android.snuffy.SnuffyApplication;

public class DebugGodToolsApplication extends SnuffyApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }

    @Override
    protected void attachBaseContext(@NonNull final Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
