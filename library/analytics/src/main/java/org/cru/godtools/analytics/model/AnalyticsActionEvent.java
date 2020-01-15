package org.cru.godtools.analytics.model;

import android.net.Uri;
import android.text.TextUtils;

import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @NonNull
    @Override
    public Uri.Builder getSnowPlowContentScoringUri() {
        return super.getSnowPlowContentScoringUri()
                .authority(SNOWPLOW_CONTENT_SCORING_URI_PATH_ACTION)
                .appendPath(getCategory())
                .appendPath(getAction());
    }

    @Override
    public String getSnowPlowPageTitle() {
        StringBuilder stringBuilder = new StringBuilder();

        if (!TextUtils.isEmpty(getCategory())) {
            stringBuilder.append(getCategory()).append(" : ");
        }
        stringBuilder.append(getAction());

        if (!TextUtils.isEmpty(getLabel())) {
            stringBuilder.append(" : ").append(getLabel());
        }
        return stringBuilder.toString();
    }
}
