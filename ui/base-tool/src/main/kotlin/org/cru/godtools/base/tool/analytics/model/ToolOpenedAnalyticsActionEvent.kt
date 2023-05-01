package org.cru.godtools.base.tool.analytics.model

import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.ACTION_OPEN
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.ACTION_OPEN_FIRST
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.user.activity.UserCounterNames

class ToolOpenedAnalyticsActionEvent(
    tool: String,
    private val type: Manifest.Type? = null,
    first: Boolean = false
) : ToolAnalyticsActionEvent(tool, action = if (first) ACTION_OPEN_FIRST else ACTION_OPEN) {
    override fun isForSystem(system: AnalyticsSystem) = when (system) {
        AnalyticsSystem.APPSFLYER -> type in setOf(Manifest.Type.ARTICLE, Manifest.Type.CYOA, Manifest.Type.TRACT)
        AnalyticsSystem.USER -> true
        else -> false
    }

    override val userCounterName = when (type) {
        Manifest.Type.LESSON -> UserCounterNames.LESSON_OPEN(tool)
        else -> UserCounterNames.TOOL_OPEN(tool)
    }
}
