package org.cru.godtools.analytics;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import org.cru.godtools.base.model.Event;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

public interface AnalyticsService {
    /* Action event names */
    String ACTION_SHARE = "Share Icon Engaged";
    String ACTION_EXIT_LINK = "Exit Link Engaged";

    /* Legacy constants */
    String CATEGORY_CONTENT_EVENT = "Content Event";

    @NonNull
    static AnalyticsService getInstance(@NonNull final Context context) {
        return AnalyticsDispatcher.getAnalyticsService(context.getApplicationContext());
    }

    @UiThread
    default void onActivityResume(@NonNull Activity activity) {}

    @UiThread
    default void onActivityPause(@NonNull Activity activity) {}

    @AnyThread
    default void onTrackShareAction() {}

    @AnyThread
    default void onTrackExitUrl(@NonNull final Uri url) {}

    @AnyThread
    default void onTrackContentEvent(@NonNull Event event) {}
}
