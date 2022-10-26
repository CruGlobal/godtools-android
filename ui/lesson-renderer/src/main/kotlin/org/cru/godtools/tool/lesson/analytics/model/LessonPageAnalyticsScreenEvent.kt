package org.cru.godtools.tool.lesson.analytics.model

import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.shared.tool.parser.model.lesson.LessonPage

class LessonPageAnalyticsScreenEvent(page: LessonPage) :
    ToolAnalyticsScreenEvent(page.toAnalyticsScreenName(), page.manifest)

private fun LessonPage.toAnalyticsScreenName() = "${manifest.code}-$position"
