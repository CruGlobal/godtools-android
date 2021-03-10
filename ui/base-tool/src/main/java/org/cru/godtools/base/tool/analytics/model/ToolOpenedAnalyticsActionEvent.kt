package org.cru.godtools.base.tool.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

class ToolOpenedAnalyticsActionEvent(first: Boolean = false) :
    AnalyticsActionEvent(if (first) "first-tool-opened" else "tool-opened", system = AnalyticsSystem.APPSFLYER)
