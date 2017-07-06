package org.cru.godtools.analytics;

import android.content.Context;

import com.google.android.gms.analytics.Tracker;

public class GoogleAnalytics {
    static Tracker tracker;

    public static Tracker getTracker(Context context) {
        if (tracker == null) {
            com.google.android.gms.analytics.GoogleAnalytics googleAnalytics =
                    com.google.android.gms.analytics.GoogleAnalytics.getInstance(context);
            googleAnalytics.setLocalDispatchPeriod(GoogleAnalyticsConfig.DISPATCH_PERIOD);
            googleAnalytics.setDryRun(GoogleAnalyticsConfig.IS_DRY_RUN);

            return googleAnalytics.newTracker(BuildConfig.GOOGLE_ANALYTICS_CLIENT_ID);
        }

        return tracker;
    }
}
