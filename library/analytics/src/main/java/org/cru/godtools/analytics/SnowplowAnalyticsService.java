package org.cru.godtools.analytics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.events.ScreenView;
import com.snowplowanalytics.snowplow.tracker.events.Structured;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.cru.godtools.analytics.BuildConfig.SNOWPLOW_ENDPOINT;

class SnowplowAnalyticsService implements AnalyticsService {
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
    }

    @Nullable
    private static SnowplowAnalyticsService sInstance;
    @NonNull
    static synchronized AnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new SnowplowAnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    /* BEGIN tracking methods */

    @Override
    public void onTrackScreen(@NonNull final String screen, @Nullable final Locale locale) {
        mAnalyticsExecutor.execute(() -> mSnowPlowTracker.track(ScreenView.builder().name(screen).build()));
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
}
