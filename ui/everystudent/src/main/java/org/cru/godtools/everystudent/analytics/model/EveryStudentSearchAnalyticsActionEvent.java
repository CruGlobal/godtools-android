package org.cru.godtools.everystudent.analytics.model;

import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.analytics.model.AnalyticsSystem;

import androidx.annotation.NonNull;

public class EveryStudentSearchAnalyticsActionEvent extends AnalyticsActionEvent {
    private static final String CATEGORY_EVERYSTUDENT_SEARCH = "searchbar";
    private static final String ACTION_EVERYSTUDENT_SEARCH = "tap";

    public EveryStudentSearchAnalyticsActionEvent(@NonNull final String query) {
        super(CATEGORY_EVERYSTUDENT_SEARCH, ACTION_EVERYSTUDENT_SEARCH, query);
    }

    @Override
    public boolean isForSystem(@NonNull final AnalyticsSystem system) {
        return system == AnalyticsSystem.GOOGLE || system == AnalyticsSystem.SNOWPLOW;
    }
}
