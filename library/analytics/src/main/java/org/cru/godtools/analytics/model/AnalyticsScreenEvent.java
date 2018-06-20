package org.cru.godtools.analytics.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;

public class AnalyticsScreenEvent extends AnalyticsBaseEvent {
    @NonNull
    private final String mScreen;
    @Nullable
    private final Locale mLocale;

    protected AnalyticsScreenEvent() {
        this("", null);
    }

    public AnalyticsScreenEvent(@NonNull final String screen) {
        this(screen, null);
    }

    public AnalyticsScreenEvent(@NonNull final String screen, @Nullable final Locale locale) {
        mScreen = screen;
        mLocale = locale;
    }

    @NonNull
    public String getScreen() {
        return mScreen;
    }

    @Nullable
    public Locale getLocale() {
        return mLocale;
    }
}
