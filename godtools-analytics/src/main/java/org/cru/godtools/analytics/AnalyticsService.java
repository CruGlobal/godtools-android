package org.cru.godtools.analytics;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.cru.godtools.base.model.Event;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AnalyticsService {
    /* Screen event names */
    public static final String SCREEN_HOME = "Home";

    /* Legacy constants */
    public static final String SCREEN_EVERYSTUDENT = "EveryStudent";
    public static final String CATEGORY_MENU = "Menu Event";
    public static final String CATEGORY_CONTENT_EVENT = "Content Event";
    private static final int DIMENSION_SCREEN_NAME = 1;
    private static final int DIMENSION_LANGUAGE = 2;
    private Tracker mTracker = null;

    private AnalyticsService(@NonNull final Context context) {

        mTracker = GoogleAnalytics.getTracker(context);

        EventBus.getDefault().register(this);
    }

    @Nullable
    private static AnalyticsService sInstance;
    @NonNull
    public static synchronized AnalyticsService getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new AnalyticsService(context.getApplicationContext());
        }

        return sInstance;
    }

    public void trackScreen(@NonNull final String screen) {
        mTracker.setScreenName(screen);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void screenView(@NonNull final String name, @NonNull final String language) {
        mTracker.setScreenName(name);
        mTracker.send(new HitBuilders.ScreenViewBuilder()
                .setCustomDimension(DIMENSION_SCREEN_NAME, name)
                .setCustomDimension(DIMENSION_LANGUAGE, language)
                .build());

    }

    public void settingChanged(@NonNull final String category, @NonNull final String event) {

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(event)
                .setLabel(event)
                .build());

    }

    public void menuEvent(@NonNull final String item) {

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_MENU)
                .setAction(item)
                .setLabel(item)
                .build());

    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void trackContentEvent(@NonNull final Event event) {
        mTracker.send(new HitBuilders.EventBuilder()
                              .setCategory(CATEGORY_CONTENT_EVENT)
                              .setAction(event.id.namespace + ":" + event.id.name)
                              .build());
    }
}
