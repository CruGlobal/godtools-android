package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

internal const val ACTION_TUTORIAL_ONBOARDING_TRAINING = "On-Boarding More"
internal const val ACTION_TUTORIAL_ONBOARDING_FINISH = "On-Boarding Start"

private const val ADOBE_TUTORIAL_ONBOARDING_TRAINING = "onboarding_more"
private const val ADOBE_TUTORIAL_ONBOARDING_FINISH = "onboarding_start"

class TutorialAnalyticsActionEvent(action: String) : AnalyticsActionEvent(category = null, action = action) {
    override fun isForSystem(system: AnalyticsSystem): Boolean {
        return system == AnalyticsSystem.ADOBE || system == AnalyticsSystem.FACEBOOK
    }

    override val adobeSiteSection get() = ADOBE_SITE_SECTION_TUTORIAL
    override val adobeAttributes = mutableMapOf<String?, Any>().apply {
        when (action) {
            ACTION_TUTORIAL_ONBOARDING_TRAINING -> put(ADOBE_TUTORIAL_ONBOARDING_TRAINING, 1)
            ACTION_TUTORIAL_ONBOARDING_FINISH -> put(ADOBE_TUTORIAL_ONBOARDING_FINISH, 1)
        }
    }
}
