package org.cru.godtools.tool.cyoa.analytics.appsflyer

import android.content.Context
import java.util.Locale
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.cru.godtools.base.ui.createCyoaActivityIntent

internal object CyoaAppsFlyerDeepLinkResolver : AppsFlyerDeepLinkResolver {
    override fun resolve(context: Context, deepLinkValue: String) = deepLinkValue
        .takeIf { it.startsWith("tool|cyoa|") }
        ?.split("|")?.takeIf { it.size >= 4 }
        ?.let { context.createCyoaActivityIntent(it[2], Locale.forLanguageTag(it[3])) }
}
