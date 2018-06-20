package org.cru.godtools.analytics.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;
import java.util.Map;

public class AnalyticsActionEvent extends AnalyticsBaseEvent {
    @Nullable
    private final String mCategory;
    @NonNull
    private final String mAction;
    @Nullable
    private final String mLabel;

    protected AnalyticsActionEvent() {
        this(null, "", null, null);
    }

    public AnalyticsActionEvent(@Nullable final String category, @NonNull final String action) {
        this(category, action, null, null);
    }

    public AnalyticsActionEvent(@Nullable final String category, @NonNull final String action,
                                @Nullable final String label) {
        this(category, action, label, null);
    }

    public AnalyticsActionEvent(@Nullable final String category, @NonNull final String action,
                                @Nullable final String label, @Nullable final Locale locale) {
        super(locale);
        mCategory = category;
        mAction = action;
        mLabel = label;
    }

    @Nullable
    public String getCategory() {
        return mCategory;
    }

    @NonNull
    public String getAction() {
        return mAction;
    }

    @Nullable
    public String getLabel() {
        return mLabel;
    }

    @Nullable
    public Map<String, ?> getAttributes() {
        return null;
    }
}
