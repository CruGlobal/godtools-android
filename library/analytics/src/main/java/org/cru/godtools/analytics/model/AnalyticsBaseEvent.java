package org.cru.godtools.analytics.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

public abstract class AnalyticsBaseEvent {
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
}
