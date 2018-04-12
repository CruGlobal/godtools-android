package org.cru.godtools;

import com.evernote.android.job.JobManager;

import org.cru.godtools.analytics.AnalyticsEventBusIndex;
import org.cru.godtools.analytics.EventBusAnalyticsHelper;
import org.cru.godtools.api.GodToolsApi;
import org.cru.godtools.base.app.BaseGodToolsApplication;
import org.cru.godtools.download.manager.DownloadManagerEventBusIndex;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.model.event.ModelEventEventBusIndex;
import org.cru.godtools.model.loader.ModelLoaderEventBusIndex;
import org.cru.godtools.sync.job.SyncJobCreator;
import org.cru.godtools.tract.TractEventBusIndex;
import org.cru.godtools.tract.service.FollowupService;
import org.greenrobot.eventbus.EventBus;

import static org.cru.godtools.config.BuildConfig.MOBILE_CONTENT_API;

public class GodToolsApplication extends BaseGodToolsApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        // configure the API
        GodToolsApi.configure(this, MOBILE_CONTENT_API);

        // configure eventbus
        configureEventbus();

        // start the Job Manager
        JobManager.create(this).addJobCreator(new SyncJobCreator());

        // Initialize tool manager
        GodToolsDownloadManager.getInstance(this);
        FollowupService.start(this);
    }

    private void configureEventbus() {
        EventBus.builder()
                .addIndex(new AppEventBusIndex())
                .addIndex(new AnalyticsEventBusIndex())
                .addIndex(new DownloadManagerEventBusIndex())
                .addIndex(new ModelEventEventBusIndex())
                .addIndex(new ModelLoaderEventBusIndex())
                .addIndex(new TractEventBusIndex())
                .installDefaultEventBus();

        EventBusAnalyticsHelper.getInstance(this);
    }
}
