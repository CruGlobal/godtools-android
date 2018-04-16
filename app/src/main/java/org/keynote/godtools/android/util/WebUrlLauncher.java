package org.keynote.godtools.android.util;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;

import org.cru.godtools.R;

public class WebUrlLauncher {
    public static void openUrl(@NonNull final Context context, @NonNull final Uri url) {
        new CustomTabsIntent.Builder()
                .setShowTitle(false)
                .enableUrlBarHiding()
                // XXX: we use gt_blue_dark to force white text & action buttons for now
                .setToolbarColor(ContextCompat.getColor(context, R.color.gt_blue_dark))
                .setInstantAppsEnabled(true)
                .build().launchUrl(context, url);
    }
}
