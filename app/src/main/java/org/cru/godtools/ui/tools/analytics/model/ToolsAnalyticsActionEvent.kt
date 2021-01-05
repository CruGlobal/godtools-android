package org.cru.godtools.ui.tools.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

object ToolOpenTapAnalyticsActionEvent : AnalyticsActionEvent("tool_open_tap", system = AnalyticsSystem.FIREBASE)
object AboutToolButtonAnalyticsActionEvent :
    AnalyticsActionEvent("about_tool_open_button", system = AnalyticsSystem.FIREBASE)
