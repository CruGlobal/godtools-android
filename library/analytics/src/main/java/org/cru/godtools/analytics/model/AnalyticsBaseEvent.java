package org.cru.godtools.analytics.model;

import android.support.annotation.NonNull;

public abstract class AnalyticsBaseEvent {
    /**
     * Return whether or not this Analytics event should be tracked in the specified service
     */
    public boolean isForSystem(@NonNull final AnalyticsSystem system) {
        return true;
    }
}
