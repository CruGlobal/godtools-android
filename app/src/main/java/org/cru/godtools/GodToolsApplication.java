package org.cru.godtools;

import com.evernote.android.job.JobManager;

import org.cru.godtools.analytics.AnalyticsEventBusIndex;
import org.cru.godtools.base.app.BaseGodToolsApplication;
import org.cru.godtools.download.manager.DownloadManagerEventBusIndex;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.model.event.ModelEventEventBusIndex;
import org.cru.godtools.model.loader.ModelLoaderEventBusIndex;
import org.cru.godtools.sync.job.SyncJobCreator;
import org.cru.godtools.tract.TractEventBusIndex;
import org.cru.godtools.tract.service.FollowupService;
import org.greenrobot.eventbus.EventBus;

public class GodToolsApplication extends BaseGodToolsApplication {
    @Override
    public void onCreate() {
        super.onCreate();

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
    }
}
