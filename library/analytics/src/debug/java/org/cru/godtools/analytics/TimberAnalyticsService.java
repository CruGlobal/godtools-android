package org.cru.godtools.analytics;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.base.model.Event;

import java.util.Locale;

import timber.log.Timber;

public class TimberAnalyticsService implements AnalyticsService {
    @Override
    public void onActivityResume(@NonNull final Activity activity) {
        Timber.tag("AnalyticsService")
                .d("onActivityResume(%s)", activity.getClass());
    }

    @Override
    public void onActivityPause(@NonNull final Activity activity) {
        Timber.tag("AnalyticsService")
                .d("onActivityPause(%s)", activity.getClass());
    }

    @Override
    public void onTrackContentEvent(@NonNull final Event event) {
        Timber.tag("AnalyticsService")
                .d("onTrackContentEvent(%s:%s)", event.id.namespace, event.id.name);
    }

    @Override
    public void onTrackScreen(@NonNull final String screen, @Nullable final Locale locale) {
        Timber.tag("AnalyticsService")
                .d("onTrackScreen('%s', %s)", screen, locale);
    }

    @Override
    public void onTrackTractPage(@NonNull final String tract, @NonNull final Locale locale, final int page,
                                 @Nullable final Integer card) {
        Timber.tag("AnalyticsService")
                .d("onTrackTractPage('%s', %s, %d, %d)", tract, locale, page, card);
    }
}
