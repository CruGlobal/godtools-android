package org.cru.godtools.tool.optInNotification.analytics.model

import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsScreenNames
import org.cru.godtools.shared.tool.parser.model.lesson.LessonPage

class LessonPageAnalyticsScreenEvent(page: LessonPage) :
    ToolAnalyticsScreenEvent(ToolAnalyticsScreenNames.forLessonPage(page), page.manifest)
