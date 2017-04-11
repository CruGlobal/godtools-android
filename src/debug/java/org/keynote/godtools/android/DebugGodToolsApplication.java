package org.keynote.godtools.android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.AndroidExcludedRefs;
import com.squareup.leakcanary.LeakCanary;

import org.ccci.gto.android.common.leakcanary.CrashlyticsLeakService;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

public class DebugGodToolsApplication extends SnuffyApplication {
    @Override
    public void onCreate() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        initLeakCanary();

        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }

    @Override
    protected void attachBaseContext(@NonNull final Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initLeakCanary() {
        LeakCanary.refWatcher(this)
                .listenerServiceClass(CrashlyticsLeakService.class)
                .excludedRefs(AndroidExcludedRefs.createAppDefaults().build())
                .buildAndInstall();
    }
}
