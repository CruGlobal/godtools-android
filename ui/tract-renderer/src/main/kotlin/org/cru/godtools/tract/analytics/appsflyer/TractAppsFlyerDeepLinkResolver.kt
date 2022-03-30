package org.cru.godtools.tract.analytics.appsflyer

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.Locale
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.cru.godtools.base.ui.createTractActivityIntent
import org.cru.godtools.tract.activity.TractActivity
import org.cru.godtools.tract.util.isTractDeepLink

object TractAppsFlyerDeepLinkResolver : AppsFlyerDeepLinkResolver {
    override fun resolve(context: Context, uri: Uri?, data: Map<String, String?>) = when {
        uri?.isTractDeepLink() == true -> Intent(Intent.ACTION_VIEW, uri, context, TractActivity::class.java)
        else -> null
    }

    override fun resolve(context: Context, deepLinkValue: String) = deepLinkValue.split("|")
        .takeIf { it.size >= 4 && it[0] == "tool" && it[1] == "tract" }
        ?.let {
            context.createTractActivityIntent(
                it[2],
                Locale.forLanguageTag(it[3]),
                page = it.getOrNull(4)?.toIntOrNull() ?: 0
            )
        }
}
