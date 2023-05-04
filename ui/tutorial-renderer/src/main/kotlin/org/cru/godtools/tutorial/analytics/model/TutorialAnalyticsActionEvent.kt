package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.analytics.TutorialAnalyticsAppSectionNames

class TutorialAnalyticsActionEvent(action: String) : AnalyticsActionEvent(action = action) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.FIREBASE

    override val appSection get() = TutorialAnalyticsAppSectionNames.forAction(action) ?: APP_SECTION_TUTORIAL
}
