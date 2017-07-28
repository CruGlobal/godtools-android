package org.cru.godtools.base.app;

import android.app.Application;

import com.newrelic.agent.android.NewRelic;

import static org.cru.godtools.base.app.BuildConfig.NEW_RELIC_API_KEY;

public class BaseGodToolsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable application monitoring
        NewRelic.withApplicationToken(NEW_RELIC_API_KEY).start(this);
    }
}
