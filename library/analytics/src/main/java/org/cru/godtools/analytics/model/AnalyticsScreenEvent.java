package org.cru.godtools.analytics.model;

import android.net.Uri;

import java.util.Locale;

import javax.annotation.concurrent.Immutable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@Immutable
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

    /* Adobe Site Sections */
    private static final String SITE_SECTION_TOOLS = "tools";
    private static final String SITE_SECTION_MENU = "menu";
    private static final String SITE_SUB_SECTION_ADD_TOOLS = "add tools";
    private static final String SITE_SUB_SECTION_LANGUAGE_SETTINGS = "language settings";

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

    @Nullable
    @Override
    public String getAdobeSiteSection() {
        switch (mScreen) {
            case SCREEN_FIND_TOOLS:
            case SCREEN_TOOL_DETAILS:
                return SITE_SECTION_TOOLS;
            case SCREEN_LANGUAGE_SETTINGS:
            case SCREEN_LANGUAGE_SELECTION:
            case SCREEN_ABOUT:
            case SCREEN_HELP:
            case SCREEN_CONTACT_US:
            case SCREEN_SHARE_GODTOOLS:
            case SCREEN_SHARE_STORY:
            case SCREEN_TERMS_OF_USE:
            case SCREEN_PRIVACY_POLICY:
            case SCREEN_COPYRIGHT:
                return SITE_SECTION_MENU;
        }
        return null;
    }

    @Nullable
    @Override
    public String getAdobeSiteSubSection() {
        switch (mScreen) {
            case SCREEN_TOOL_DETAILS:
                return SITE_SUB_SECTION_ADD_TOOLS;
            case SCREEN_LANGUAGE_SELECTION:
                return SITE_SUB_SECTION_LANGUAGE_SETTINGS;
        }
        return null;
    }

    @NonNull
    @Override
    public Uri.Builder getSnowPlowContentScoringUri() {
        return super.getSnowPlowContentScoringUri()
                .authority(SNOWPLOW_CONTENT_SCORING_URI_PATH_SCREEN)
                .appendPath(getScreen());
    }

    @Override
    public String getSnowPlowPageTitle() {
        return getScreen();
    }
}
