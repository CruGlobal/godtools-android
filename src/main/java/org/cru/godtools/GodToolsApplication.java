package org.cru.godtools;

import android.app.Application;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.newrelic.agent.android.NewRelic;
import com.squareup.picasso.Picasso;

import org.ccci.gto.android.common.api.okhttp3.util.OkHttpClientUtil;
import org.cru.godtools.analytics.AnalyticsEventBusIndex;
import org.cru.godtools.download.manager.DownloadManagerEventBusIndex;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.model.event.ModelEventEventBusIndex;
import org.cru.godtools.model.loader.ModelLoaderEventBusIndex;
import org.cru.godtools.sync.SyncEventBusIndex;
import org.cru.godtools.sync.job.SyncJobCreator;
import org.cru.godtools.sync.service.FollowupService;
import org.cru.godtools.tract.TractEventBusIndex;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.BuildConfig;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;

public class GodToolsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Enable crash reporting
        Fabric.with(this, new Crashlytics());
        NewRelic.withApplicationToken(BuildConfig.NEW_RELIC_API_KEY).start(this);

        // configure eventbus
        configureEventbus();

        // initialize several support libraries
        Picasso.setSingletonInstance(picassoBuilder().build());

        // start the Job Manager
        JobManager.create(this).addJobCreator(new SyncJobCreator());

        // Initialize tool manager
        GodToolsDownloadManager.getInstance(this);
        FollowupService.start(this);
    }

    @NonNull
    protected Picasso.Builder picassoBuilder() {
        final Picasso.Builder builder = new Picasso.Builder(this);

        // use OkHttp3 for the downloader
        final OkHttpClient okhttp = OkHttpClientUtil.attachGlobalInterceptors(new OkHttpClient.Builder())
                .cache(OkHttp3Downloader.createDefaultCache(this))
                .build();
        builder.downloader(new OkHttp3Downloader(okhttp));

        return builder;
    }

    private void configureEventbus() {
        EventBus.builder()
                .addIndex(new AppEventBusIndex())
                .addIndex(new AnalyticsEventBusIndex())
                .addIndex(new DownloadManagerEventBusIndex())
                .addIndex(new ModelEventEventBusIndex())
                .addIndex(new ModelLoaderEventBusIndex())
                .addIndex(new SyncEventBusIndex())
                .addIndex(new TractEventBusIndex())
                .installDefaultEventBus();
    }
}
