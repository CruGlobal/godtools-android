package org.cru.godtools.analytics;

import android.content.Context;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.wrappers.InstantApps;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.analytics.model.AnalyticsSystem;
import org.cru.godtools.base.model.Event;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

class GoogleAnalyticsService implements AnalyticsService {
    /* Custom dimensions */
    private static final int DIMENSION_TOOL = 1;
    private static final int DIMENSION_LANGUAGE = 2;
    private static final int DIMENSION_APP_TYPE = 4;

    private static final String VALUE_APP_TYPE_INSTANT = "instant";
    private static final String VALUE_APP_TYPE_INSTALLED = "installed";

    private final Tracker mTracker;
    private final String mAppType;

    private GoogleAnalyticsService(@NonNull final Context context) {
        mTracker = GoogleAnalytics.getInstance(context).newTracker(BuildConfig.GOOGLE_ANALYTICS_CLIENT_ID);
        mAppType = InstantApps.isInstantApp(context) ? VALUE_APP_TYPE_INSTANT : VALUE_APP_TYPE_INSTALLED;

        EventBus.getDefault().register(this);
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

    @AnyThread
    @Subscribe
    public void onAnalyticsScreenEvent(@NonNull final AnalyticsScreenEvent event) {
        if (event.isForSystem(AnalyticsSystem.GOOGLE)) {
            onTrackScreen(event.getScreen(), event.getLocale());
        }
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
        sendEvent(event);
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
        sendEvent(eventBuilder);
    }

    @Override
    public void onTrackEveryStudentSearch(@NonNull final String query) {
        mTracker.setScreenName(SCREEN_EVERYSTUDENT_SEARCH);
        sendEvent(new HitBuilders.EventBuilder()
                              .setCategory(CATEGORY_EVERYSTUDENT_SEARCH)
                              .setAction(ACTION_EVERYSTUDENT_SEARCH)
                              .setLabel(query));
    }

    private void sendEvent(@NonNull final HitBuilders.HitBuilder<? extends HitBuilders.HitBuilder> event) {
        event.setCustomDimension(DIMENSION_APP_TYPE, mAppType);
        mTracker.send(event.build());
    }
}
