package org.cru.godtools.base.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import org.cru.godtools.ui.R
import timber.log.Timber

fun Context.openUrl(url: Uri) = try {
    CustomTabsIntent.Builder()
        .setShowTitle(false)
        .setUrlBarHidingEnabled(true)
        .setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder()
                // XXX: we use gt_blue_dark to force white text & action buttons for now
                .setToolbarColor(ContextCompat.getColor(this, R.color.gt_blue_dark))
                .build()
        )
        .setInstantAppsEnabled(true)
        .build().launchUrl(this, url)
    true
} catch (e: ActivityNotFoundException) {
    Timber.tag("WebUrlLauncher")
        .d(e, "Unable to open url: %s", url)
    Toast.makeText(applicationContext, getString(R.string.toast_unable_to_launch_activity, url), Toast.LENGTH_LONG)
        .show()
    false
}
