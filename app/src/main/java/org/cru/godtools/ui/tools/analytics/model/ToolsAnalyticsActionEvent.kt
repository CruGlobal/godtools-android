package org.cru.godtools.ui.tools.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

object ToolOpenTapAnalyticsActionEvent : AnalyticsActionEvent("cru.tool_open_tap", system = AnalyticsSystem.ADOBE)
object ToolOpenButtonAnalyticsActionEvent : AnalyticsActionEvent("cru.tool_open_button", system = AnalyticsSystem.ADOBE)
object AboutToolButtonAnalyticsActionEvent :
    AnalyticsActionEvent("cru.about_tool_button", system = AnalyticsSystem.ADOBE)
