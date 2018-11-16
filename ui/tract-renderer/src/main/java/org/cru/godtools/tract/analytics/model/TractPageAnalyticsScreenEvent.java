package org.cru.godtools.tract.analytics.model;

import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent;

import java.util.Locale;

import javax.annotation.concurrent.Immutable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@Immutable
public class TractPageAnalyticsScreenEvent extends ToolAnalyticsScreenEvent {
    public TractPageAnalyticsScreenEvent(@NonNull final String tract, @NonNull final Locale locale, final int page,
                                         @Nullable final Integer card) {
        super(tractPageToScreenName(tract, page, card), tract, locale);
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
