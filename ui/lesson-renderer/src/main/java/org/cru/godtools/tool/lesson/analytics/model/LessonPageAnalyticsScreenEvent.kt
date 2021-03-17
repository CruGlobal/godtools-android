package org.cru.godtools.tool.lesson.analytics.model

import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.xml.model.lesson.LessonPage

class LessonPageAnalyticsScreenEvent(page: LessonPage) :
    ToolAnalyticsScreenEvent(page.toAnalyticsScreenName(), page.manifest) {
    override fun isForSystem(system: AnalyticsSystem) = system != AnalyticsSystem.APPSFLYER
}

private fun LessonPage.toAnalyticsScreenName() = "${manifest.code}-$position"
