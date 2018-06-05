package org.keynote.godtools.android.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import org.cru.godtools.R;

import timber.log.Timber;

public class WebUrlLauncher {
    public static boolean openUrl(@NonNull final Context context, @NonNull final Uri url) {
        try {
            new CustomTabsIntent.Builder()
                    .setShowTitle(false)
                    .enableUrlBarHiding()
                    // XXX: we use gt_blue_dark to force white text & action buttons for now
                    .setToolbarColor(ContextCompat.getColor(context, R.color.gt_blue_dark))
                    .setInstantAppsEnabled(true)
                    .build().launchUrl(context, url);
            return true;
        } catch (final ActivityNotFoundException e) {
            Timber.tag("WebUrlLauncher")
                    .d(e, "Unable to open url: %s", url);
            Toast.makeText(context, context.getString(R.string.error_unable_to_launch_url, url), Toast.LENGTH_LONG)
                    .show();
            return false;
        }
    }
}
