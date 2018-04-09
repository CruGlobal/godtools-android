package org.cru.godtools.analytics;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.base.model.Event;

public interface AnalyticsService {
    /* Screen event names */
    String SCREEN_HOME = "Home";
    String SCREEN_ADD_TOOLS = "Add Tools";
    String SCREEN_TOOL_DETAILS = "Tool Info";
    String SCREEN_LANGUAGE_SETTINGS = "Language Settings";
    String SCREEN_LANGUAGE_SELECTION = "Select Language";
    String SCREEN_MENU = "Menu";
    String SCREEN_ABOUT = "About";
    String SCREEN_HELP = "Help";
    String SCREEN_CONTACT_US = "Contact Us";
    String SCREEN_SHARE_GODTOOLS = "Share App";
    String SCREEN_SHARE_STORY = "Share Story";
    String SCREEN_TERMS_OF_USE = "Terms of Use";
    String SCREEN_PRIVACY_POLICY = "Privacy Policy";
    String SCREEN_COPYRIGHT = "Copyright Info";

    /* Custom dimensions */
    int DIMENSION_TOOL = 1;
    int DIMENSION_LANGUAGE = 2;

    /* Legacy constants */
    String SCREEN_EVERYSTUDENT = "EveryStudent";
    String SCREEN_EVERYSTUDENT_SEARCH = "everystudent-search";
    String CATEGORY_CONTENT_EVENT = "Content Event";
    String CATEGORY_EVERYSTUDENT_SEARCH = "searchbar";
    String ACTION_EVERYSTUDENT_SEARCH = "tap";

    static AnalyticsService getInstance(@NonNull final Context context) {
        return DefaultAnalyticsService.getInstance(context.getApplicationContext());
    }

    @AnyThread
    default void onActivityResume(@NonNull Activity activity) {}

    @AnyThread
    default void onActivityPause(@NonNull Activity activity) {}

    @AnyThread
    default void onTrackScreen(@NonNull String screen) {
        onTrackScreen(screen, null);
    }

    @AnyThread
    default void onTrackScreen(@NonNull String screen, @Nullable String language) {}

    @AnyThread
    default void onTrackContentEvent(@NonNull Event event) {}

    @AnyThread
    default void onTrackEveryStudentSearch(@NonNull String query) {}
}
