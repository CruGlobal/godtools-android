package org.cru.godtools.analytics;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

import timber.log.Timber;

public class TimberAnalyticsService implements AnalyticsService {
    @Override
    public void onActivityResume(@NonNull final Activity activity) {
        Timber.d("onActivityResume(%s)", activity.getClass());
    }

    @Override
    public void onActivityPause(@NonNull final Activity activity) {
        Timber.d("onActivityPause(%s)", activity.getClass());
    }

    @Override
    public void onTrackScreen(@NonNull final String screen, @Nullable final Locale locale) {
        Timber.d("onTrackScreen('%s', %s)", screen, locale);
    }

    @Override
    public void onTrackTractPage(@NonNull final String tract, @NonNull final Locale locale, final int page,
                                 @Nullable final Integer card) {
        Timber.d("onTrackTractPage('%s', %s, %d, %d)", tract, locale, page, card);
    }
}
