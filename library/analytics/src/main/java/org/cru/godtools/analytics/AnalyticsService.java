package org.cru.godtools.analytics;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import org.cru.godtools.base.model.Event;

import java.util.Locale;

public interface AnalyticsService {
    /* Screen event names */
    String SCREEN_HOME = "Home";
    String SCREEN_FIND_TOOLS = "Find Tools";
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

    /* Action event names */
    String ACTION_SHARE = "Share Icon Engaged";
    String ACTION_EXIT_LINK = "Exit Link Engaged";
    String ACTION_TOGGLE_LANGUAGE = "Parallel Language Toggled";

    /* Legacy constants */
    String SCREEN_EVERYSTUDENT = "EveryStudent";
    String SCREEN_EVERYSTUDENT_SEARCH = "everystudent-search";
    String CATEGORY_CONTENT_EVENT = "Content Event";
    String CATEGORY_EVERYSTUDENT_SEARCH = "searchbar";
    String ACTION_EVERYSTUDENT_SEARCH = "tap";

    @NonNull
    static AnalyticsService getInstance(@NonNull final Context context) {
        return AnalyticsDispatcher.getAnalyticsService(context.getApplicationContext());
    }

    @UiThread
    default void onActivityResume(@NonNull Activity activity) {}

    @UiThread
    default void onActivityPause(@NonNull Activity activity) {}

    @AnyThread
    default void onTrackScreen(@NonNull String screen) {
        onTrackScreen(screen, null);
    }

    @AnyThread
    default void onTrackScreen(@NonNull String screen, @Nullable Locale locale) {
        onTrackScreen(screen);
    }

    @AnyThread
    default void onTrackTractPage(@NonNull final String tract, @NonNull final Locale locale, final int page,
                                  @Nullable final Integer card) {
        onTrackScreen(tractPageToScreenName(tract, page, card), locale);
    }

    @AnyThread
    default void onTrackShareAction() {}

    @AnyThread
    default void onTrackExitUrl(@NonNull final Uri url) {}

    @AnyThread
    default void onTrackToggleLanguage(@NonNull final Locale newLocale) {}

    @AnyThread
    default void onTrackContentEvent(@NonNull Event event) {}

    @AnyThread
    default void onTrackEveryStudentSearch(@NonNull String query) {}

    @NonNull
    static String tractPageToScreenName(@NonNull final String tract, final int page, @Nullable final Integer card) {
        final StringBuilder name = new StringBuilder(tract).append('-').append(page);
        if (card != null) {
            if (card >= 0 && card < 26) {
                // convert card index to letter 'a'-'z'
                name.append((char) (97 + card));
            } else {
                name.append('-').append(card);
            }
        }
        return name.toString();
    }
}
