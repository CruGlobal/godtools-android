package org.cru.godtools.analytics;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import org.cru.godtools.base.model.Event;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EventBusAnalyticsHelper {
    @NonNull
    private final AnalyticsService mAnalyticsService;

    EventBusAnalyticsHelper(@NonNull final AnalyticsService service) {
        mAnalyticsService = service;
        EventBus.getDefault().register(this);
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContentEvent(@NonNull final Event event) {
        mAnalyticsService.onTrackContentEvent(event);
    }
}
