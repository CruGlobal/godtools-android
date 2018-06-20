package org.cru.godtools.analytics;

import android.content.Context;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.events.ScreenView;
import com.snowplowanalytics.snowplow.tracker.events.Structured;

import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.analytics.model.AnalyticsBaseEvent;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.analytics.model.AnalyticsSystem;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.cru.godtools.analytics.BuildConfig.SNOWPLOW_ENDPOINT;

public class SnowplowAnalyticsService implements AnalyticsService {
    /* SnowPlow value constants */
    private static final String SNOWPLOW_APP_ID = "GodTools";
    private static final String SNOWPLOW_NAMESPACE = "GodToolsSnowPlowAndroidTracker";

    /**
     * Single thread executor to serialize events on a background thread.
     */
    private final Executor mAnalyticsExecutor = Executors.newSingleThreadExecutor();
    private Tracker mSnowPlowTracker;

    private SnowplowAnalyticsService(@NonNull final Context context) {
        mAnalyticsExecutor.execute(() -> {
            Tracker.close();
            // XXX: creating an Emitter will initialize the event store database on whichever thread the emitter is
            // XXX: created on. Because of this we serialize all Snowplow interactions on a background thread.
            final Emitter emitter = new Emitter.EmitterBuilder(SNOWPLOW_ENDPOINT, context).build();
            mSnowPlowTracker = new Tracker.TrackerBuilder(emitter, SNOWPLOW_NAMESPACE, SNOWPLOW_APP_ID, context)
                    .base64(false)
                    .mobileContext(true)
                    .lifecycleEvents(true)
                    .build();
        });

        EventBus.getDefault().register(this);
    }

    @Nullable
    private static SnowplowAnalyticsService sInstance;
    @NonNull
    public static synchronized AnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new SnowplowAnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    /* BEGIN tracking methods */

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

    @Override
    public void onTrackEveryStudentSearch(@NonNull final String query) {
        mAnalyticsExecutor.execute(() -> mSnowPlowTracker.track(Structured.builder()
                                                                        .category(CATEGORY_EVERYSTUDENT_SEARCH)
                                                                        .action(ACTION_EVERYSTUDENT_SEARCH)
                                                                        .label(query)
                                                                        .build()));
    }

    /* END tracking methods */

    private void handleScreenEvent(@NonNull final AnalyticsScreenEvent event) {
        trackScreen(event.getScreen(), event.getLocale());
    }

    private void handleActionEvent(@NonNull final AnalyticsActionEvent event) {
        final Structured.Builder builder = Structured.builder()
                .category(event.getCategory())
                .action(event.getAction());
        final String label = event.getLabel();
        if (label != null) {
            builder.label(label);
        }

        mAnalyticsExecutor.execute(() -> mSnowPlowTracker.track(builder.build()));
    }

    @AnyThread
    private void trackScreen(@NonNull final String screen, @Nullable final Locale locale) {
        mAnalyticsExecutor.execute(() -> mSnowPlowTracker.track(ScreenView.builder().name(screen).build()));
    }
}
