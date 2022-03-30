package org.cru.godtools.article.analytics.appsflyer

import android.content.Context
import java.util.Locale
import org.cru.godtools.analytics.appsflyer.AppsFlyerDeepLinkResolver
import org.cru.godtools.base.ui.createArticlesIntent

internal object ArticlesAppsFlyerDeepLinkResolver : AppsFlyerDeepLinkResolver {
    override fun resolve(context: Context, deepLinkValue: String) = deepLinkValue.split("|")
        .takeIf { it.size >= 4 && it[0] == "tool" && it[1] == "article" }
        ?.let { context.createArticlesIntent(it[2], Locale.forLanguageTag(it[3])) }
}
