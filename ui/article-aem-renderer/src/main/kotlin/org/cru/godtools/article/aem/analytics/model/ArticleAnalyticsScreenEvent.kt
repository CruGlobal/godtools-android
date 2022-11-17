package org.cru.godtools.article.aem.analytics.model

import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.shared.user.activity.UserCounterNames

private const val SCREEN_ARTICLE_PREFIX = "Article : "

private const val APP_SUB_SECTION_ARTICLE = "article"

class ArticleAnalyticsScreenEvent(article: Article, tool: String?, locale: Locale?) :
    ToolAnalyticsScreenEvent("$SCREEN_ARTICLE_PREFIX${article.title}", tool, locale) {
    override fun isForSystem(system: AnalyticsSystem) = when (system) {
        AnalyticsSystem.USER -> true
        else -> super.isForSystem(system)
    }

    override val appSubSection get() = APP_SUB_SECTION_ARTICLE

    override val userCounterName = UserCounterNames.ARTICLE_OPEN(article.uri)
}
