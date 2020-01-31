package org.cru.godtools.analytics;

import android.content.Context;
import android.net.Uri;

import org.cru.godtools.base.model.Event;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;

public interface AnalyticsService {
    /* Action event names */
    String ACTION_EXIT_LINK = "Exit Link Engaged";

    @NonNull
    static AnalyticsService getInstance(@NonNull final Context context) {
        return AnalyticsDispatcher.getAnalyticsService(context.getApplicationContext());
    }

    @AnyThread
    default void onTrackExitUrl(@NonNull final Uri url) {}

    @AnyThread
    default void onTrackContentEvent(@NonNull Event event) {}
}
