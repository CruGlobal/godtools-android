package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import java.util.Locale

const val TUTORIAL_SCREEN_SITE_SECTION = "tutorial"

class TutorialAnalyticsScreenEvent(screenName: String) : AnalyticsScreenEvent(screenName, Locale.getDefault()) {
    override fun getAdobeSiteSection(): String? {
        return TUTORIAL_SCREEN_SITE_SECTION
    }
}
