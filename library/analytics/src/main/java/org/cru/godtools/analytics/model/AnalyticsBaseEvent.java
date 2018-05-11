package org.cru.godtools.analytics.model;

import android.support.annotation.NonNull;

import org.cru.godtools.analytics.AnalyticsService;

public abstract class AnalyticsBaseEvent {
    /**
     * Return whether or not this Analytics event should be tracked in the specified service
     */
    public boolean trackInService(@NonNull final AnalyticsService service) {
        return true;
    }
}
