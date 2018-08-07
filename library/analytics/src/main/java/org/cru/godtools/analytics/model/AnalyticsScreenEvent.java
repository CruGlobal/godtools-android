package org.cru.godtools.analytics.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

public class AnalyticsScreenEvent extends AnalyticsBaseEvent {
    private static final String SNOWPLOW_CONTENT_SCORING_URI_PATH_SCREEN = "screenview";

    /* Screen event names */
    public static final String SCREEN_HOME = "Home";
    public static final String SCREEN_FIND_TOOLS = "Find Tools";
    public static final String SCREEN_TOOL_DETAILS = "Tool Info";
    public static final String SCREEN_LANGUAGE_SETTINGS = "Language Settings";
    public static final String SCREEN_LANGUAGE_SELECTION = "Select Language";
    public static final String SCREEN_MENU = "Menu";
    public static final String SCREEN_ABOUT = "About";
    public static final String SCREEN_HELP = "Help";
    public static final String SCREEN_CONTACT_US = "Contact Us";
    public static final String SCREEN_SHARE_GODTOOLS = "Share App";
    public static final String SCREEN_SHARE_STORY = "Share Story";
    public static final String SCREEN_TERMS_OF_USE = "Terms of Use";
    public static final String SCREEN_PRIVACY_POLICY = "Privacy Policy";
    public static final String SCREEN_COPYRIGHT = "Copyright Info";

    @NonNull
    private final String mScreen;

    public AnalyticsScreenEvent(@NonNull final String screen) {
        this(screen, null);
    }

    public AnalyticsScreenEvent(@NonNull final String screen, @Nullable final Locale locale) {
        super(locale);
        mScreen = screen;
    }

    @NonNull
    public String getScreen() {
        return mScreen;
    }

    @Override
    public Uri.Builder getSnowPlowContentScoringUri() {
        return super.getSnowPlowContentScoringUri()
                .appendPath(SNOWPLOW_CONTENT_SCORING_URI_PATH_SCREEN)
                .appendPath(getScreen());
    }
}
