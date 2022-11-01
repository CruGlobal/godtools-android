package org.cru.godtools.base.tool.analytics.model

import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.tool.parser.model.Manifest

private const val USER_COUNTER_PREFIX = "tool_opens."

class ToolOpenedAnalyticsActionEvent(
    tool: String,
    private val type: Manifest.Type? = null,
    first: Boolean = false
) : ToolAnalyticsActionEvent(tool, action = if (first) "first-tool-opened" else "tool-opened") {
    override fun isForSystem(system: AnalyticsSystem) = when (system) {
        AnalyticsSystem.APPSFLYER -> type in setOf(Manifest.Type.ARTICLE, Manifest.Type.CYOA, Manifest.Type.TRACT)
        AnalyticsSystem.USER -> true
        else -> false
    }

    override val userCounterName = "$USER_COUNTER_PREFIX$tool"
}
