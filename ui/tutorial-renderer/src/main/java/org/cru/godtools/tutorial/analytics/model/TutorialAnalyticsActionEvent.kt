package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

internal const val ACTION_TUTORIAL_ONBOARDING_TRAINING = "onboarding_more"
internal const val ACTION_TUTORIAL_FINISH = "onboarding_start"
const val KEY_ACTION_TUTORIAL = "cru.tutorialaction"

class TutorialAnalyticsActionEvent(private val actionName: String) : AnalyticsActionEvent(null, KEY_ACTION_TUTORIAL) {
    override fun isForSystem(system: AnalyticsSystem): Boolean {
        return system == AnalyticsSystem.ADOBE || system == AnalyticsSystem.FACEBOOK
    }

    override val adobeSiteSection get() = ADOBE_SITE_SECTION_TUTORIAL
    override val adobeAttributes: Map<String?, Any> = mapOf(actionName to 1)
}
