package org.cru.godtools.article.analytics.model

import java.util.Locale
import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent

private const val SCREEN_ARTICLES_ALL = "All Articles"
private const val SCREEN_ARTICLES_CATEGORY_PREFIX = "Category : "

private const val SITE_SUB_SECTION_ARTICLES_LIST = "articles-list"

open class ArticlesAnalyticsScreenEvent(tool: String?, locale: Locale?, screen: String = SCREEN_ARTICLES_ALL) :
    ToolAnalyticsScreenEvent(screen, tool, locale) {
    override val adobeSiteSubSection get() = SITE_SUB_SECTION_ARTICLES_LIST
}

class ArticlesCategoryAnalyticsScreenEvent(tool: String, locale: Locale, category: String) :
    ArticlesAnalyticsScreenEvent(tool, locale, "$SCREEN_ARTICLES_CATEGORY_PREFIX$category")
