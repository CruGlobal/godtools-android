package org.cru.godtools.ui.tools.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

object ToolOpenTapAnalyticsActionEvent : AnalyticsActionEvent("Tool Open Tap", system = AnalyticsSystem.ADOBE) {
    override val firebaseEventName = "tool_open_tap"
}
object AboutToolButtonAnalyticsActionEvent :
    AnalyticsActionEvent("About Tool Open Button", system = AnalyticsSystem.ADOBE) {
    override val firebaseEventName = "about_tool_open_button"
}
