package org.keynote.godtools.android.googleAnalytics;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.android.BuildConfig;
import org.keynote.godtools.android.event.GodToolsEvent;
import org.keynote.godtools.android.utils.GoogleAnalytics;

/**
 * This class is used to track events for Google Analytics.
 */
public class EventTracker {
    public static final String SCREEN_EVERYSTUDENT = "EveryStudent";
    public static final String SCREEN_SETTINGS = "Settings";

    public static final String CATEGORY_MENU = "Menu Event";
    public static final String CATEGORY_CONTENT_EVENT = "Content Event";

    private static final int DIMENSION_SCREEN_NAME = 1;
    private static final int DIMENSION_LANGUAGE = 2;


    private Tracker mTracker = null;

    private static EventTracker instance;

    private EventTracker(@NonNull final Context context) {
        if(!BuildConfig.DEBUG) {
            mTracker = GoogleAnalytics.getTracker(context);
        }
        EventBus.getDefault().register(this);
    }

    @NonNull
    public static EventTracker getInstance(@NonNull final Context context) {
        synchronized (EventTracker.class) {

            if (instance == null) {
                instance = new EventTracker(context.getApplicationContext());
            }

            return instance;
        }
    }

    public void activeScreen(@NonNull final String screen) {
        if(!BuildConfig.DEBUG)
            mTracker.setScreenName(screen);
    }

    public void screenView(@NonNull final String name, @NonNull final String language) {
        if(!BuildConfig.DEBUG) {
            mTracker.setScreenName(name);
            mTracker.send(new HitBuilders.ScreenViewBuilder()
                    .setCustomDimension(DIMENSION_SCREEN_NAME, name)
                    .setCustomDimension(DIMENSION_LANGUAGE, language)
                    .build());
        }
    }

    public void settingChanged(@NonNull final String category, @NonNull final String event) {
        if(!BuildConfig.DEBUG) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(event)
                    .setLabel(event)
                    .build());
        }
    }

    public void menuEvent(@NonNull final String item) {
        if(!BuildConfig.DEBUG) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(CATEGORY_MENU)
                    .setAction(item)
                    .setLabel(item)
                    .build());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void trackContentEvent(@NonNull final GodToolsEvent event) {
        if(!BuildConfig.DEBUG) {
            final GodToolsEvent.EventID eventID = event.getEventID();
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(CATEGORY_CONTENT_EVENT)
                    .setAction(eventID.getNamespace() + ":" + eventID.getId())
                    .build());
        }
    }
}
