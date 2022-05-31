package org.cru.godtools.base.tool.analytics.model

import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

open class ToolAnalyticsActionEvent(
    private val tool: String?,
    action: String,
    locale: Locale? = null,
    systems: Collection<AnalyticsSystem> = DEFAULT_SYSTEMS
) : AnalyticsActionEvent(action, locale = locale, systems = systems) {
    override val appSection get() = tool
}
