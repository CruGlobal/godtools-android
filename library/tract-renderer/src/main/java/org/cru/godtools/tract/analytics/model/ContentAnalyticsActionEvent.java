package org.cru.godtools.tract.analytics.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;

import org.cru.godtools.analytics.AdobeAnalyticsService;
import org.cru.godtools.analytics.AnalyticsService;
import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.tract.model.AnalyticsEvent;

import java.util.Map;

public class ContentAnalyticsActionEvent extends AnalyticsActionEvent {
    @NonNull
    private final AnalyticsEvent mAnalyticsEvent;

    public ContentAnalyticsActionEvent(@NonNull final AnalyticsEvent event) {
        mAnalyticsEvent = event;
    }

    @Override
    public boolean trackInService(@NonNull final AnalyticsService service) {
        if (service instanceof AdobeAnalyticsService) {
            return mAnalyticsEvent.isForSystem(AnalyticsEvent.System.ADOBE);
        }
        return false;
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
