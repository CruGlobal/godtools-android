package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

internal const val ACTION_TUTORIAL_ONBOARDING_TRAINING = "On-Boarding More"
internal const val ACTION_TUTORIAL_ONBOARDING_FINISH = "On-Boarding Start"

private const val ADOBE_TUTORIAL_ONBOARDING_TRAINING = "cru.onboarding_more"
private const val ADOBE_TUTORIAL_ONBOARDING_FINISH = "cru.onboarding_start"

const val ADOBE_TUTORIAL_HOME_DISMISS = "cru.tutorial_home_dismiss"

class TutorialAnalyticsActionEvent(action: String) : AnalyticsActionEvent(action = action) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.ADOBE

    override val appSection get() = APP_SECTION_TUTORIAL

    override val firebaseEventName = when (action) {
        ACTION_TUTORIAL_ONBOARDING_TRAINING -> "onboarding_more"
        ACTION_TUTORIAL_ONBOARDING_FINISH -> "onboarding_start"
        else -> super.firebaseEventName
    }

    @OptIn(ExperimentalStdlibApi::class)
    override val adobeAttributes = buildMap<String, Int> {
        when (action) {
            ACTION_TUTORIAL_ONBOARDING_TRAINING -> put(ADOBE_TUTORIAL_ONBOARDING_TRAINING, 1)
            ACTION_TUTORIAL_ONBOARDING_FINISH -> put(ADOBE_TUTORIAL_ONBOARDING_FINISH, 1)
        }
    }
}
