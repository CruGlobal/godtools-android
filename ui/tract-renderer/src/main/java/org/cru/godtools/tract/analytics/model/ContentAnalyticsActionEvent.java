package org.cru.godtools.tract.analytics.model;

import com.google.common.base.Strings;

import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.analytics.model.AnalyticsSystem;
import org.cru.godtools.xml.model.AnalyticsEvent;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ContentAnalyticsActionEvent extends AnalyticsActionEvent {
    @NonNull
    private final AnalyticsEvent mAnalyticsEvent;

    public ContentAnalyticsActionEvent(@NonNull final AnalyticsEvent event) {
        mAnalyticsEvent = event;
    }

    @Override
    public boolean isForSystem(@NonNull final AnalyticsSystem system) {
        return mAnalyticsEvent.isForSystem(system);
    }

    @NonNull
    @Override
    public String getAction() {
        return Strings.nullToEmpty(mAnalyticsEvent.getAction());
    }

    @Nullable
    @Override
    public Map<String, ?> getAttributes() {
        return mAnalyticsEvent.getAttributes();
    }
}
