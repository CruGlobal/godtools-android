package org.cru.godtools.ui.dashboard.analytics.model

import android.os.Bundle
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

private const val PARAM_SOURCE = "cru_source"
private const val PARAM_TOOL = "cru_tool"

internal class DashboardToolClickedAnalyticsActionEvent(
    action: String,
    private val tool: String?,
    private val source: String? = null
) : AnalyticsActionEvent(action = action, system = AnalyticsSystem.FIREBASE) {
    companion object {
        const val ACTION_OPEN_LESSON = "dashboard_open_lesson"
        const val ACTION_OPEN_TOOL = "tool_open_tap"
        const val ACTION_OPEN_TOOL_DETAILS = "dashboard_open_details"

        const val SOURCE_FEATURED = "featured"
        const val SOURCE_SPOTLIGHT = "spotlight"
    }

    override val firebaseParams get() = Bundle().apply {
        if (source != null) putString(PARAM_SOURCE, source)
        if (tool != null) putString(PARAM_TOOL, tool)
    }
}
