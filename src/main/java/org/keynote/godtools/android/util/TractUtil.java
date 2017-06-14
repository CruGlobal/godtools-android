package org.keynote.godtools.android.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.tract.service.TractManager;

import java.util.Locale;

public final class TractUtil {
    public static void preloadNewestPublishedTract(@NonNull final Context context, final long toolId,
                                                   @Nullable final Locale locale) {
        if (locale != null) {
            final TractManager tractManager = TractManager.getInstance(context);
            tractManager.getLatestPublishedManifest(toolId, locale);
        }
    }
}
