package org.cru.godtools.analytics;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.base.model.Event;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import timber.log.Timber;

public class TimberAnalyticsService implements AnalyticsService {
    private TimberAnalyticsService() {
        EventBus.getDefault().register(this);
    }

    @Nullable
    private static TimberAnalyticsService sInstance;
    @NonNull
    public static synchronized TimberAnalyticsService getInstance() {
        if (sInstance == null) {
            sInstance = new TimberAnalyticsService();
        }

        return sInstance;
    }

    /* BEGIN lifecycle */

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

    @UiThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnalyticsScreenEvent(@NonNull final AnalyticsScreenEvent event) {
        Timber.tag("AnalyticsService")
                .d("onAnalyticsScreenEvent('%s', '%s')", event.getScreen(), event.getLocale());
    }

    @UiThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnalyticsActionEvent(@NonNull final AnalyticsActionEvent event) {
        Timber.tag("AnalyticsService")
                .d("onAnalyticsActionEvent('%s')", event.getAction());
    }

    /* END lifecycle */
}
