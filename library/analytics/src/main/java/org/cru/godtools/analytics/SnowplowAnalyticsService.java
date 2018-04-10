package org.cru.godtools.analytics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.snowplowanalytics.snowplow.tracker.Emitter;
import com.snowplowanalytics.snowplow.tracker.Emitter.EmitterBuilder;
import com.snowplowanalytics.snowplow.tracker.Tracker;
import com.snowplowanalytics.snowplow.tracker.Tracker.TrackerBuilder;
import com.snowplowanalytics.snowplow.tracker.events.ScreenView;
import com.snowplowanalytics.snowplow.tracker.events.Structured;

import java.util.Locale;

import static org.cru.godtools.analytics.BuildConfig.SNOWPLOW_ENDPOINT;

class SnowplowAnalyticsService implements AnalyticsService {
    /* SnowPlow value constants */
    private static final String SNOWPLOW_APP_ID = "GodTools";
    private static final String SNOWPLOW_NAMESPACE = "GodToolsSnowPlowAndroidTracker";

    @NonNull
    private final Tracker mSnowPlowTracker;

    private SnowplowAnalyticsService(@NonNull final Context context) {
        Tracker.close();
        final Emitter emitter = new EmitterBuilder(SNOWPLOW_ENDPOINT, context).build();
        mSnowPlowTracker = new TrackerBuilder(emitter, SNOWPLOW_NAMESPACE, SNOWPLOW_APP_ID, context)
//                .level(LogLevel.DEBUG)
                .base64(false)
                .mobileContext(true)
                .lifecycleEvents(true)
                .build();
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
        mSnowPlowTracker.track(ScreenView.builder().name(screen).build());
    }

    @Override
    public void onTrackEveryStudentSearch(@NonNull final String query) {
        mSnowPlowTracker.track(Structured.builder()
                                       .category(CATEGORY_EVERYSTUDENT_SEARCH)
                                       .action(ACTION_EVERYSTUDENT_SEARCH)
                                       .label(query)
                                       .build());
    }

    /* END tracking methods */
}
