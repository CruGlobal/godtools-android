package org.cru.godtools.analytics;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.wrappers.InstantApps;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.analytics.model.AnalyticsBaseEvent;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.analytics.model.AnalyticsSystem;
import org.cru.godtools.base.model.Event;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    public void onAnalyticsEvent(@NonNull final AnalyticsBaseEvent event) {
        if (event.isForSystem(AnalyticsSystem.GOOGLE)) {
            if (event instanceof AnalyticsScreenEvent) {
                handleScreenEvent((AnalyticsScreenEvent) event);
            } else if (event instanceof AnalyticsActionEvent) {
                handleActionEvent((AnalyticsActionEvent) event);
            }
        }
    }

    @Override
    public void onTrackContentEvent(@NonNull final Event event) {
        // build event
        final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_CONTENT_EVENT)
                .setAction(event.id.namespace + ":" + event.id.name);

        // send event
        sendEvent(eventBuilder, event.locale);
    }

    @AnyThread
    private void handleScreenEvent(@NonNull final AnalyticsScreenEvent event) {
        mTracker.setScreenName(event.getScreen());
        sendEvent(new HitBuilders.ScreenViewBuilder(), event.getLocale());
    }

    @AnyThread
    private void handleActionEvent(@NonNull final AnalyticsActionEvent event) {
        final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(event.getCategory())
                .setAction(event.getAction());
        final String label = event.getLabel();
        if (label != null) {
            eventBuilder.setLabel(label);
        }

        sendEvent(eventBuilder, event.getLocale());
    }

    private void sendEvent(@NonNull final HitBuilders.HitBuilder<? extends HitBuilders.HitBuilder> event,
                           @Nullable final Locale locale) {
        event.setCustomDimension(DIMENSION_APP_TYPE, mAppType);
        if (locale != null) {
            event.setCustomDimension(DIMENSION_LANGUAGE, LocaleCompat.toLanguageTag(locale));
        }
        mTracker.send(event.build());
    }
}
