package org.cru.godtools.tool.lesson.analytics.model

import android.os.Bundle
import androidx.core.os.bundleOf
import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsActionEvent
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.ACTION_LESSON_FEEDBACK
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.PARAM_LESSON_FEEDBACK_HELPFUL
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.PARAM_LESSON_FEEDBACK_PAGE_REACHED
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.PARAM_LESSON_FEEDBACK_READINESS
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.VALUE_LESSON_FEEDBACK_HELPFUL_NO
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.VALUE_LESSON_FEEDBACK_HELPFUL_YES

internal class LessonFeedbackAnalyticsEvent(tool: String, locale: Locale, private val args: Bundle) :
    ToolAnalyticsActionEvent(tool, ACTION_LESSON_FEEDBACK, locale = locale, systems = setOf(AnalyticsSystem.FIREBASE)) {
    companion object {
        const val PARAM_HELPFUL = PARAM_LESSON_FEEDBACK_HELPFUL
        const val PARAM_READINESS = PARAM_LESSON_FEEDBACK_READINESS
        const val PARAM_PAGE_REACHED = PARAM_LESSON_FEEDBACK_PAGE_REACHED

        const val VALUE_HELPFUL_YES = VALUE_LESSON_FEEDBACK_HELPFUL_YES
        const val VALUE_HELPFUL_NO = VALUE_LESSON_FEEDBACK_HELPFUL_NO
    }

    constructor(tool: String, locale: Locale, pageReached: Int, helpful: Boolean?, readiness: Int) : this(
        tool,
        locale,
        args = bundleOf(
            PARAM_PAGE_REACHED to pageReached,
            PARAM_HELPFUL to when (helpful) {
                true -> VALUE_HELPFUL_YES
                false -> VALUE_HELPFUL_NO
                null -> null
            },
            PARAM_READINESS to readiness.coerceIn(1, 10),
        ),
    )

    override val firebaseParams get() = Bundle().apply { putAll(args) }
}
