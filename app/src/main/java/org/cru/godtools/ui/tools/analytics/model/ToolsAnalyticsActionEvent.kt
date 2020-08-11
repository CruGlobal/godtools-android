package org.cru.godtools.ui.tools.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

sealed class ToolsAnalyticsActionEvent(action: String) : AnalyticsActionEvent(action) {
    override fun isForSystem(system: AnalyticsSystem) = (system == AnalyticsSystem.ADOBE)
}

object ToolOpenTap : ToolsAnalyticsActionEvent("cru.tool_open_tap")
object ToolOpenButton : ToolsAnalyticsActionEvent("cru.tool_open_button")
object AboutToolButton : ToolsAnalyticsActionEvent("cru.about_tool_button")
