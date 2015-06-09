package org.keynote.godtools.android.googleAnalytics;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.keynote.godtools.android.snuffy.SnuffyApplication;

/**
 * This class is used to track events for Google Analytics.
 */
public class EventTracker
{
    private EventTracker()
    {
    }

    /**
     * Track a screen visit
     */
    public static void track(SnuffyApplication app, String screenName, String dimension2)
    {
        Tracker tracker = app.getTracker();
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.AppViewBuilder()
                .setCustomDimension(1, screenName)
                .setCustomDimension(2, dimension2)
                .build());
    }

    /**
     * Track an event
     */
    public static void track(SnuffyApplication app, String screenName, String category, String event)
    {
        Tracker tracker = app.getTracker();
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(event)
                .setLabel(event)
                .build());
    }
}
