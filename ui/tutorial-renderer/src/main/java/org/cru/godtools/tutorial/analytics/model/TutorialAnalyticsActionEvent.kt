package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import java.util.HashMap

private const val TUTORIAL_SITE_SECTION = "tutorial"


class TutorialAnalyticsActionEvent(private val actionName: String) : AnalyticsActionEvent(null, actionName) {

    override fun isForSystem(system: AnalyticsSystem): Boolean {
        return system == AnalyticsSystem.ADOBE || system == AnalyticsSystem.APPSEE || system == AnalyticsSystem.FACEBOOK
    }

    override fun getAdobeSiteSection(): String? {
        return TUTORIAL_SITE_SECTION
    }

    override fun getAttributes(): MutableMap<String, *> {
        val attrs = HashMap<String, Any>()
        attrs[actionName] = 1
        return attrs
    }

    companion object {
        const val TUTORIAL_MORE_ACTION = "onboarding_more"
        const val TUTORIAL_START_ACTION = "onboarding_start"
    }
}
