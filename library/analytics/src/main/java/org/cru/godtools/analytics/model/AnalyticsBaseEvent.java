package org.cru.godtools.analytics.model;

import android.net.Uri;

import java.util.Locale;

import javax.annotation.concurrent.Immutable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@Immutable
public abstract class AnalyticsBaseEvent {
    private static final String SNOWPLOW_CONTENT_SCORING_URI_SCHEME = "godtools";

    @Nullable
    private final Locale mLocale;

    AnalyticsBaseEvent(@Nullable final Locale locale) {
        mLocale = locale;
    }

    /**
     * Return whether or not this Analytics event should be tracked in the specified service
     */
    public boolean isForSystem(@NonNull final AnalyticsSystem system) {
        return true;
    }

    @Nullable
    public Locale getLocale() {
        return mLocale;
    }

    @Nullable
    public String getAdobeSiteSection() {
        return null;
    }

    @Nullable
    public String getAdobeSiteSubSection() {
        return null;
    }

    public Uri.Builder getSnowPlowContentScoringUri() {
        return new Uri.Builder()
                .scheme(SNOWPLOW_CONTENT_SCORING_URI_SCHEME);
    }

    public String getSnowPlowPageTitle() {
        return null;
    }
}
