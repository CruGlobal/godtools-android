package org.cru.godtools.analytics;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.google.common.util.concurrent.Futures;
import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.events.Event;
import com.snowplowanalytics.snowplow.tracker.events.ScreenView;
import com.snowplowanalytics.snowplow.tracker.events.Structured;

import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.analytics.model.AnalyticsBaseEvent;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.analytics.model.AnalyticsSystem;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import static org.cru.godtools.analytics.BuildConfig.SNOWPLOW_ENDPOINT;

public class SnowplowAnalyticsService {
    /* SnowPlow value constants */
    private static final String SNOWPLOW_APP_ID = "GodTools";
    private static final String SNOWPLOW_NAMESPACE = "GodToolsSnowPlowAndroidTracker";

    private final RunnableFuture<Tracker> mSnowPlowTracker;

    @AnyThread
    private SnowplowAnalyticsService(@NonNull final Context context) {
        mSnowPlowTracker = new FutureTask<>(() -> {
            Tracker.close();
            // XXX: creating an Emitter will initialize the event store database on whichever thread the emitter is
            // XXX: created on. Because of this we initialize Snowplow in a background task
            final Emitter emitter = new Emitter.EmitterBuilder(SNOWPLOW_ENDPOINT, context).build();
            return new Tracker.TrackerBuilder(emitter, SNOWPLOW_NAMESPACE, SNOWPLOW_APP_ID, context)
                    .base64(false)
                    .mobileContext(true)
                    .lifecycleEvents(true)
                    .build();
        });

        AsyncTask.THREAD_POOL_EXECUTOR.execute(mSnowPlowTracker);
        EventBus.getDefault().register(this);
    }

    @Nullable
    private static SnowplowAnalyticsService sInstance;
    @NonNull
    @AnyThread
    public static synchronized SnowplowAnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new SnowplowAnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAnalyticsEvent(@NonNull final AnalyticsBaseEvent event) {
        if (event.isForSystem(AnalyticsSystem.SNOWPLOW)) {
            if (event instanceof AnalyticsScreenEvent) {
                handleScreenEvent((AnalyticsScreenEvent) event);
            } else if (event instanceof AnalyticsActionEvent) {
                handleActionEvent((AnalyticsActionEvent) event);
            }
        }
    }

    @WorkerThread
    private void handleScreenEvent(@NonNull final AnalyticsScreenEvent event) {
        sendEvent(ScreenView.builder().name(event.getScreen()).build());
    }

    @WorkerThread
    private void handleActionEvent(@NonNull final AnalyticsActionEvent event) {
        final Structured.Builder builder = Structured.builder()
                .category(event.getCategory())
                .action(event.getAction());
        final String label = event.getLabel();
        if (label != null) {
            builder.label(label);
        }

        sendEvent(builder.build());
    }

    @WorkerThread
    private void sendEvent(@NonNull final Event event) {
        Futures.getUnchecked(mSnowPlowTracker).track(event);
    }
}
