package org.cru.godtools.analytics.model

sealed class OpenToolsAnalyticsActionEvent(action: String) : AnalyticsActionEvent(action) {
    override fun isForSystem(system: AnalyticsSystem): Boolean {
        return system == AnalyticsSystem.APPSFLYER
    }
}

object ToolOpened : OpenToolsAnalyticsActionEvent("tool-opened")
object FirstToolOpened : OpenToolsAnalyticsActionEvent("first-tool-opened")
