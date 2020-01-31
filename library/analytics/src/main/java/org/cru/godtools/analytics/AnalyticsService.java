package org.cru.godtools.analytics;

import android.content.Context;

import androidx.annotation.NonNull;

public interface AnalyticsService {
    @NonNull
    static AnalyticsService getInstance(@NonNull final Context context) {
        return AnalyticsDispatcher.getAnalyticsService(context.getApplicationContext());
    }
}
