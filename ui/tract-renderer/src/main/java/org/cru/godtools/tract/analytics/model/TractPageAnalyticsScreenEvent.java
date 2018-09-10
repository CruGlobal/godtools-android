package org.cru.godtools.tract.analytics.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.analytics.model.AnalyticsScreenEvent;

import java.util.Locale;

import javax.annotation.concurrent.Immutable;

@Immutable
public class TractPageAnalyticsScreenEvent extends AnalyticsScreenEvent {
    @NonNull
    private final String mTract;

    public TractPageAnalyticsScreenEvent(@NonNull final String tract, @NonNull final Locale locale, final int page,
                                         @Nullable final Integer card) {
        super(tractPageToScreenName(tract, page, card), locale);
        mTract = tract;
    }

    @Nullable
    @Override
    public String getAdobeSiteSection() {
        return mTract;
    }

    @NonNull
    private static String tractPageToScreenName(@NonNull final String tract, final int page,
                                                @Nullable final Integer card) {
        final StringBuilder name = new StringBuilder(tract).append('-').append(page);
        if (card != null) {
            if (card >= 0 && card < 26) {
                // convert card index to letter 'a'-'z'
                name.append((char) (97 + card));
            } else {
                name.append('-').append(card);
            }
        }
        return name.toString();
    }
}
