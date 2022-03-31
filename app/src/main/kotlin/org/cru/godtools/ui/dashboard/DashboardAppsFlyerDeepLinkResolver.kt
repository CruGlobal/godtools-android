package org.cru.godtools.ui.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.cru.godtools.base.ui.createDashboardIntent
import org.cru.godtools.base.ui.dashboard.Page
import org.keynote.godtools.android.activity.MainActivity

internal object DashboardAppsFlyerDeepLinkResolver : AppsFlyerDeepLinkResolver {
    override fun resolve(context: Context, uri: Uri?, data: Map<String, String?>) = when {
        uri?.isDashboardLessonsDeepLink() == true -> Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)
        else -> null
    }

    override fun resolve(context: Context, deepLinkValue: String) = deepLinkValue.split("|")
        .takeIf { it[0] == "dashboard" }
        ?.let {
            when (it.getOrNull(1)) {
                "tools" -> Page.ALL_TOOLS
                "lessons" -> Page.LESSONS
                "home" -> Page.FAVORITE_TOOLS
                else -> Page.FAVORITE_TOOLS
            }
        }
        ?.let { context.createDashboardIntent(it) }
}
