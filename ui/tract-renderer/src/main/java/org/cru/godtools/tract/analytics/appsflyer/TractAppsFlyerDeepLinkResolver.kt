package org.cru.godtools.tract.analytics.appsflyer

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.cru.godtools.tract.activity.TractActivity
import org.cru.godtools.tract.util.isTractDeepLink

object TractAppsFlyerDeepLinkResolver : AppsFlyerDeepLinkResolver {
    override fun resolve(context: Context, uri: Uri?, data: Map<String, String?>) = when {
        uri?.isTractDeepLink() == true -> Intent(Intent.ACTION_VIEW, uri, context, TractActivity::class.java)
        else -> null
    }
}
