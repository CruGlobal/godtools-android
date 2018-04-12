package org.cru.godtools.analytics;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.cru.godtools.base.model.Event;

import java.util.Locale;

class GoogleAnalyticsService implements AnalyticsService {
    private final Tracker mTracker;

    private GoogleAnalyticsService(@NonNull final Context context) {
        mTracker = GoogleAnalytics.getInstance(context).newTracker(BuildConfig.GOOGLE_ANALYTICS_CLIENT_ID);
    }

    @Nullable
    private static GoogleAnalyticsService sInstance;
    @NonNull
    static synchronized GoogleAnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new GoogleAnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    @Override
    public void onTrackScreen(@NonNull final String screen, @Nullable final Locale locale) {
        // build event
        final HitBuilders.ScreenViewBuilder event = new HitBuilders.ScreenViewBuilder();
        if (locale != null) {
            event.setCustomDimension(DIMENSION_LANGUAGE, LocaleCompat.toLanguageTag(locale));
        }

        // send event
        mTracker.setScreenName(screen);
        mTracker.send(event.build());
    }

    @Override
    public void onTrackContentEvent(@NonNull final Event event) {
        // build event
        final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_CONTENT_EVENT)
                .setAction(event.id.namespace + ":" + event.id.name);
        if (event.locale != null) {
            eventBuilder.setCustomDimension(DIMENSION_LANGUAGE, LocaleCompat.toLanguageTag(event.locale));
        }

        // send event
        mTracker.send(eventBuilder.build());
    }

    @Override
    public void onTrackEveryStudentSearch(@NonNull final String query) {
        mTracker.setScreenName(SCREEN_EVERYSTUDENT_SEARCH);
        mTracker.send(new HitBuilders.EventBuilder()
                              .setCategory(CATEGORY_EVERYSTUDENT_SEARCH)
                              .setAction(ACTION_EVERYSTUDENT_SEARCH)
                              .setLabel(query)
                              .build());
    }
}
