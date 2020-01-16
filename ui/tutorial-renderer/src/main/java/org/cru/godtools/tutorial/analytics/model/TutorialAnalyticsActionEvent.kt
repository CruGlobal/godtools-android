package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

internal const val ACTION_TUTORIAL_ONBOARDING_TRAINING = "onboarding_more"
internal const val ACTION_TUTORIAL_FINISH = "onboarding_start"

class TutorialAnalyticsActionEvent(action: String) : AnalyticsActionEvent(null, action) {
    override fun isForSystem(system: AnalyticsSystem): Boolean {
        return system == AnalyticsSystem.ADOBE || system == AnalyticsSystem.FACEBOOK
    }

    override val adobeSiteSection get() = ADOBE_SITE_SECTION_TUTORIAL
    override fun getAttributes() = mapOf(action to 1)
}
