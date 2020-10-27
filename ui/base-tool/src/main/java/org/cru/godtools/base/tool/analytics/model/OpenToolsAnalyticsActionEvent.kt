package org.cru.godtools.base.tool.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

object ToolOpenedAnalyticsActionEvent : AnalyticsActionEvent("tool-opened", system = AnalyticsSystem.APPSFLYER)
object FirstToolOpenedAnalyticsActionEvent :
    AnalyticsActionEvent("first-tool-opened", system = AnalyticsSystem.APPSFLYER)
