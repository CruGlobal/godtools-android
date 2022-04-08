package org.cru.godtools.ui.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.cru.godtools.base.ui.createDashboardIntent

internal object DashboardAppsFlyerDeepLinkResolver : AppsFlyerDeepLinkResolver {
    override fun resolve(context: Context, uri: Uri?, data: Map<String, String?>) = when {
        uri?.isDashboardLessonsDeepLink() == true ->
            Intent(Intent.ACTION_VIEW, uri, context, DashboardActivity::class.java)
        else -> null
    }

    override fun resolve(context: Context, deepLinkValue: String) = deepLinkValue.split("|")
        .takeIf { it[0] == "dashboard" }
        ?.let { findPageByUriPathSegment(it.getOrNull(1)) }
        ?.let { context.createDashboardIntent(it) }
}
