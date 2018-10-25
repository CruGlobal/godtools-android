package org.cru.godtools.feature.tract;

import android.os.AsyncTask;

import org.cru.godtools.api.GodToolsApi;
import org.cru.godtools.base.app.BaseGodToolsApplication;
import org.cru.godtools.init.content.task.InitialContentTasks;
import org.cru.godtools.tract.TractEventBusIndex;
import org.cru.godtools.tract.service.FollowupService;
import org.greenrobot.eventbus.EventBusBuilder;

import androidx.annotation.NonNull;

import static org.cru.godtools.config.BuildConfig.MOBILE_CONTENT_API;

public class GodToolsTractApplication extends BaseGodToolsApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        // load initial content
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new InitialContentTasks(this));
    }

    @Override
    protected void configureApis() {
        GodToolsApi.configure(this, MOBILE_CONTENT_API);
    }

    @NonNull
    protected EventBusBuilder configureEventBus(@NonNull final EventBusBuilder builder) {
        return super.configureEventBus(builder)
                .addIndex(new TractEventBusIndex());
    }

    @Override
    protected void startServices() {
        super.startServices();
        FollowupService.start(this);
    }
}
