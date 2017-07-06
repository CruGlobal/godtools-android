package org.keynote.godtools.android.snuffy;

import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.newrelic.agent.android.NewRelic;
import com.squareup.picasso.Picasso;

import org.ccci.gto.android.common.api.okhttp3.util.OkHttpClientUtil;
import org.cru.godtools.AppEventBusIndex;
import org.cru.godtools.analytics.AnalyticsEventBusIndex;
import org.cru.godtools.model.events.ModelEventsEventBusIndex;
import org.cru.godtools.sync.SyncEventBusIndex;
import org.cru.godtools.sync.service.FollowupService;
import org.cru.godtools.sync.service.GodToolsDownloadManager;
import org.cru.godtools.tract.TractEventBusIndex;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.BuildConfig;
import org.keynote.godtools.android.RenderAppConfig;
import org.keynote.godtools.renderer.crureader.BaseAppConfig;
import org.keynote.godtools.renderer.crureader.RenderApp;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;

public class SnuffyApplication extends RenderApp {

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

        // Initialize tool manager
        GodToolsDownloadManager.getInstance(this);
        FollowupService.start(this);
    }

    @Override
    public BaseAppConfig getBaseAppConfig() {
        return new RenderAppConfig();
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
                .addIndex(new ModelEventsEventBusIndex())
                .addIndex(new SyncEventBusIndex())
                .addIndex(new TractEventBusIndex())
                .installDefaultEventBus();
    }
}
