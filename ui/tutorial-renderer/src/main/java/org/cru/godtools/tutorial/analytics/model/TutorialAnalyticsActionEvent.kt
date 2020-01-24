package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

internal const val ACTION_TUTORIAL_ONBOARDING_TRAINING = "onboarding_more"
internal const val ACTION_NAME_TUTORIAL_ONBOARDING_TRAINING = "On-Boarding More"
internal const val ACTION_TUTORIAL_FINISH = "onboarding_start"
internal const val ACTION_NAME_TUTORIAL_FINISH = "On-Boarding Start"

class TutorialAnalyticsActionEvent(private val actionName: String, private val actionTitle: String) : AnalyticsActionEvent(null, actionTitle) {
    override fun isForSystem(system: AnalyticsSystem): Boolean {
        return system == AnalyticsSystem.ADOBE || system == AnalyticsSystem.FACEBOOK
    }

    override val adobeSiteSection get() = ADOBE_SITE_SECTION_TUTORIAL
    override val adobeAttributes: Map<String?, Any> = mapOf(actionName to 1)
}
