package org.cru.godtools.analytics.model

import android.os.Bundle

private const val PARAM_SOURCE = "cru_source"
private const val PARAM_TOOL = "cru_tool"

internal class OpenAnalyticsActionEvent(
    action: String,
    private val tool: String?,
    private val source: String
) : AnalyticsActionEvent(action = action, system = AnalyticsSystem.FIREBASE) {
    companion object {
        const val ACTION_OPEN_LESSON = "open_lesson"
        const val ACTION_OPEN_TOOL = "open_tool"
        const val ACTION_OPEN_TOOL_DETAILS = "open_details"

        const val SOURCE_LESSONS = "lessons"
        const val SOURCE_FAVORITE = "favorite_tools"
        const val SOURCE_ALL_TOOLS = "all_tools"
        const val SOURCE_FEATURED = "featured"
        const val SOURCE_SPOTLIGHT = "spotlight"
        const val SOURCE_TOOL_DETAILS = "tool_details"
        const val SOURCE_VARIANTS = "versions"
    }

    override val firebaseParams get() = Bundle().apply {
        putString(PARAM_SOURCE, source)
        if (tool != null) putString(PARAM_TOOL, tool)
    }

    override fun equals(other: Any?) = when {
        this === other -> true
        javaClass != other?.javaClass -> false
        !super.equals(other) -> false
        other !is OpenAnalyticsActionEvent -> false
        tool != other.tool -> false
        source != other.source -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (tool?.hashCode() ?: 0)
        result = 31 * result + source.hashCode()
        return result
    }
}
