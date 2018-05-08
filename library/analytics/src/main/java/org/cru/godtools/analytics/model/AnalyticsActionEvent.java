package org.cru.godtools.analytics.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

public class AnalyticsActionEvent extends AnalyticsBaseEvent {
    @NonNull
    private final String mAction;

    protected AnalyticsActionEvent() {
        mAction = "";
    }

    public AnalyticsActionEvent(@NonNull final String action) {
        mAction = action;
    }

    @NonNull
    public String getAction() {
        return mAction;
    }

    @Nullable
    public Map<String, ?> getAttributes() {
        return null;
    }
}
