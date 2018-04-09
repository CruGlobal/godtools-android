package org.cru.godtools.feature.tract;

import org.cru.godtools.api.GodToolsApi;
import org.cru.godtools.base.app.BaseGodToolsApplication;

import static org.cru.godtools.config.BuildConfig.MOBILE_CONTENT_API;

public class GodToolsTractApplication extends BaseGodToolsApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        // configure the API
        GodToolsApi.configure(this, MOBILE_CONTENT_API);
    }
}
