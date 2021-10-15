package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

internal const val ACTION_TUTORIAL_ONBOARDING_LINK_ARTICLES = "onboarding_link_articles"
internal const val ACTION_TUTORIAL_ONBOARDING_LINK_LESSONS = "onboarding_link_lessons"
internal const val ACTION_TUTORIAL_ONBOARDING_LINK_TOOLS = "onboarding_link_tools"
internal const val ACTION_TUTORIAL_ONBOARDING_FINISH = "onboarding_start"

const val TUTORIAL_HOME_DISMISS = "tutorial_home_dismiss"

class TutorialAnalyticsActionEvent(action: String) : AnalyticsActionEvent(action = action) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.FIREBASE

    override val appSection get() = APP_SECTION_TUTORIAL
}
