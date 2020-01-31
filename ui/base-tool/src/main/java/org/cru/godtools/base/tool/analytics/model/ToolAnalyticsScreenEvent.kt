package org.cru.godtools.base.tool.analytics.model

import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import java.util.Locale

const val SCREEN_CATEGORIES = "Categories"

open class ToolAnalyticsScreenEvent(
    screen: String,
    private val tool: String?,
    locale: Locale? = null
) : AnalyticsScreenEvent(screen, locale) {
    override val adobeSiteSection get() = tool
}
