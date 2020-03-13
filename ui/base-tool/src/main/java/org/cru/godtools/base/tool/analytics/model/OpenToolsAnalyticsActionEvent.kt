package org.cru.godtools.base.tool.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

sealed class OpenToolsAnalyticsActionEvent(action: String) : AnalyticsActionEvent(action) {
    override fun isForSystem(system: AnalyticsSystem): Boolean {
        return system == AnalyticsSystem.APPSFLYER
    }
}

object ToolOpened : OpenToolsAnalyticsActionEvent("tool-opened")
object FirstToolOpened : OpenToolsAnalyticsActionEvent("first-tool-opened")
