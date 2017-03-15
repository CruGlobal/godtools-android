package org.keynote.godtools.android.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent;

/**
 * This class is used to track events for Google Analytics.
 */
public class EventTracker {

    public static final String SCREEN_EVERYSTUDENT = "EveryStudent";
    public static final String SCREEN_SETTINGS = "Settings";
    public static final String CATEGORY_MENU = "Menu Event";
    public static final String CATEGORY_CONTENT_EVENT = "Content Event";
    private static final String TAG = "EventTracker";
    private static final int DIMENSION_SCREEN_NAME = 1;
    private static final int DIMENSION_LANGUAGE = 2;
    private static EventTracker instance;
    private Tracker mTracker = null;

    private EventTracker(@NonNull final Context context) {

        mTracker = GoogleAnalytics.getTracker(context);

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

        mTracker.setScreenName(screen);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void trackContentEvent(@NonNull final GodToolsEvent event) {
        Log.i(TAG, "EventBus: trackerContentEvent " + event.toString());

        final GodToolsEvent.EventID eventID = event.getEventID();
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(CATEGORY_CONTENT_EVENT)
                .setAction(eventID.getNamespace() + ":" + eventID.getId())
                .build());

    }

}
