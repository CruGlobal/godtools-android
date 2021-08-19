package org.cru.godtools.ui.dashboard

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.keynote.godtools.android.activity.MainActivity

internal object DashboardAppsFlyerDeepLinkResolver : AppsFlyerDeepLinkResolver {
    override fun resolve(context: Context, uri: Uri?, data: Map<String, String?>) = when {
        uri?.isDashboardLessonsDeepLink() == true -> Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)
        else -> null
    }
}
