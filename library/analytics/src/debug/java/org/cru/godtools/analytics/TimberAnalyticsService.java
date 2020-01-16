package org.cru.godtools.analytics;

import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.analytics.model.AnalyticsScreenEvent;
import org.cru.godtools.base.model.Event;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

public class TimberAnalyticsService {
    private TimberAnalyticsService() {
        EventBus.getDefault().register(this);
    }

    @Nullable
    private static TimberAnalyticsService sInstance;

    @NonNull
    public static synchronized TimberAnalyticsService start() {
        if (sInstance == null) {
            sInstance = new TimberAnalyticsService();
        }

        return sInstance;
    }

    // region Events
    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackContentEvent(@NonNull final Event event) {
        Timber.tag("AnalyticsService")
                .d("onTrackContentEvent(%s:%s)", event.id.namespace, event.id.name);
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnalyticsScreenEvent(@NonNull final AnalyticsScreenEvent event) {
        Timber.tag("AnalyticsService")
                .d("onAnalyticsScreenEvent('%s', '%s')", event.getScreen(), event.getLocale());
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAnalyticsActionEvent(@NonNull final AnalyticsActionEvent event) {
        Timber.tag("AnalyticsService")
                .d("onAnalyticsActionEvent('%s')", event.getAction());
    }
    // endregion Events
}
