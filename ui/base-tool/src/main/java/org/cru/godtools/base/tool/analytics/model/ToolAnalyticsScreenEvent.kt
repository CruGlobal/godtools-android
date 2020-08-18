package org.cru.godtools.base.tool.analytics.model

import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsScreenEvent

const val SCREEN_CATEGORIES = "Categories"

open class ToolAnalyticsScreenEvent(
    screen: String,
    private val tool: String?,
    locale: Locale? = null
) : AnalyticsScreenEvent(screen, locale) {
    override val adobeSiteSection get() = tool
}
