package org.cru.godtools.analytics;

import org.cru.godtools.base.model.Event;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

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
