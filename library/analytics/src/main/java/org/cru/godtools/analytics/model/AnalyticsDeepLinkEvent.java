package org.cru.godtools.analytics.model;

import android.net.Uri;

import androidx.annotation.NonNull;

public class AnalyticsDeepLinkEvent extends AnalyticsBaseEvent {
    @NonNull
    private final Uri mDeepLink;

    public AnalyticsDeepLinkEvent(@NonNull final Uri deepLink) {
        super(null);
        mDeepLink = deepLink;
    }

    @NonNull
    public Uri getDeepLink() {
        return mDeepLink;
    }
}
