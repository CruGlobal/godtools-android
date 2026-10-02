package org.cru.godtools.ui.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri

// Custom Data key set on push notifications sent from the Firebase Notifications composer
internal const val EXTRA_NOTIFICATION_DEEP_LINK = "deep_link"

/**
 * Builds a VIEW intent for the deep link attached to a push notification. The intent is restricted to GodTools,
 * and null is returned when no GodTools activity handles the deep link, so notification data can't open other apps.
 */
internal fun Context.createNotificationDeepLinkIntent(intent: Intent): Intent? {
    val deepLink = intent.getStringExtra(EXTRA_NOTIFICATION_DEEP_LINK)
    if (deepLink.isNullOrBlank()) return null

    return Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
        .addCategory(Intent.CATEGORY_BROWSABLE)
        .setPackage(packageName)
        .takeIf { it.resolveActivity(packageManager) != null }
}
