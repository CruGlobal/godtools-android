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

    /* Adobe analytics key constants */
    String ADOBE_APP_NAME = "cru.appname";
    String ADOBE_LOGGED_IN_STATUS = "cru.loggedinstatus";
    String ADOBE_MARKETING_CLOUD_ID = "cru.mcid";
    String ADOBE_SCREEN_NAME = "cru.screenname";
    String ADOBE_PREVIOUS_SCREEN_NAME = "cru.previousscreenname";

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
    default void trackScreen(@NonNull String screen) {
        trackScreen(screen, null);
    }

    @AnyThread
    default void trackScreen(@NonNull String screen, @Nullable String language) {}

    @AnyThread
    default void trackEveryStudentSearch(@NonNull String query) {}

    @AnyThread
    default void onTrackContentEvent(@NonNull Event event) {}
}
