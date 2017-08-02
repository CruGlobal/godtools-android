package org.cru.godtools.base.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.android.instantapps.InstantApps;
import com.newrelic.agent.android.NewRelic;

import io.fabric.sdk.android.Fabric;

import static org.cru.godtools.base.app.BuildConfig.NEW_RELIC_API_KEY;

public class BaseGodToolsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable application monitoring
        initializeCrashlytics();
        NewRelic.withApplicationToken(NEW_RELIC_API_KEY).start(this);
    }

    private void initializeCrashlytics() {
        Fabric.with(this, new Crashlytics());
        Crashlytics.setBool("InstantApp", InstantApps.isInstantApp(this));
    }
}
