package org.cru.godtools.analytics.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Locale;
import java.util.Map;

public class AnalyticsActionEvent extends AnalyticsBaseEvent {
    private static final String SNOWPLOW_CONTENT_SCORING_URI_PATH_ACTION = "action";

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

    @Override
    public Uri.Builder getSnowPlowContentScoringUri() {
        return super.getSnowPlowContentScoringUri()
                .appendPath(SNOWPLOW_CONTENT_SCORING_URI_PATH_ACTION)
                .appendPath(getCategory())
                .appendPath(getAction());
    }
}
