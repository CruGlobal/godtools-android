package org.cru.godtools.tool.lesson.analytics.model

import android.os.Bundle
import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsActionEvent

private const val ACTION_LESSON_FEEDBACK = "lesson_feedback"

internal class LessonFeedbackAnalyticsEvent(tool: String, locale: Locale, private val args: Bundle) :
    ToolAnalyticsActionEvent(tool, ACTION_LESSON_FEEDBACK, locale = locale, systems = setOf(AnalyticsSystem.FIREBASE)) {
    companion object {
        const val PARAM_HELPFUL = "helpful"
        const val PARAM_READINESS = "readiness"
        const val PARAM_PAGE_REACHED = "page_reached"

        const val VALUE_HELPFUL_YES = "yes"
        const val VALUE_HELPFUL_NO = "no"
    }

    override val firebaseParams get() = Bundle().apply { putAll(args) }
}
