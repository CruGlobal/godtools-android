package org.cru.godtools.ui.tools.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

object ToolOpenTapAnalyticsActionEvent : AnalyticsActionEvent("Tool Open Tap", system = AnalyticsSystem.ADOBE)
object ToolOpenButtonAnalyticsActionEvent : AnalyticsActionEvent("Tool Open Button", system = AnalyticsSystem.ADOBE)
object AboutToolButtonAnalyticsActionEvent :
    AnalyticsActionEvent("About Tool Open Button", system = AnalyticsSystem.ADOBE)
