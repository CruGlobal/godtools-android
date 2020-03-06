package org.cru.godtools.analytics.model

sealed class OpenToolsAnalyticActionEvent(action: String) : AnalyticsActionEvent(action) {
    override fun isForSystem(system: AnalyticsSystem): Boolean {
        return system == AnalyticsSystem.APPSFLYER
    }
}

object ToolOpened : OpenToolsAnalyticActionEvent("tool-opened")
object FirstToolOpened : OpenToolsAnalyticActionEvent("first-tool-opened")
