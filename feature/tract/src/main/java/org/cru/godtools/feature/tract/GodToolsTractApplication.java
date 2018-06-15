package org.cru.godtools.feature.tract;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.cru.godtools.api.GodToolsApi;
import org.cru.godtools.base.app.BaseGodToolsApplication;
import org.cru.godtools.init.content.task.InitialContentTasks;
import org.cru.godtools.tract.TractEventBusIndex;
import org.greenrobot.eventbus.EventBusBuilder;

import static org.cru.godtools.config.BuildConfig.MOBILE_CONTENT_API;

public class GodToolsTractApplication extends BaseGodToolsApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        // configure the API
        GodToolsApi.configure(this, MOBILE_CONTENT_API);

        // load initial content
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new InitialContentTasks(this));
    }

    @NonNull
    protected EventBusBuilder configureEventBus(@NonNull final EventBusBuilder builder) {
        return super.configureEventBus(builder)
                .addIndex(new TractEventBusIndex());
    }
}
