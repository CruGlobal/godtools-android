package org.cru.godtools.analytics;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.base.model.Event;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EventBusAnalyticsHelper {
    @NonNull
    private final AnalyticsService mAnalyticsService;

    private EventBusAnalyticsHelper(@NonNull final Context context) {
        mAnalyticsService = AnalyticsService.getInstance(context);

        EventBus.getDefault().register(this);
    }

    @Nullable
    private static EventBusAnalyticsHelper sInstance;

    @NonNull
    public static synchronized EventBusAnalyticsHelper getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new EventBusAnalyticsHelper(context.getApplicationContext());
        }

        return sInstance;
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContentEvent(@NonNull final Event event) {
        mAnalyticsService.onTrackContentEvent(event);
    }
}
