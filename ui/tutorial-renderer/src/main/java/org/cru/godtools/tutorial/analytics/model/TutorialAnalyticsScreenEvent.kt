package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import java.util.Locale

const val TUTORIAL_SCREEN_SITE_SECTION = "tutorial"

class TutorialAnalyticsScreenEvent(screenName: String) : AnalyticsScreenEvent(screenName, Locale.getDefault()) {
    override val adobeSiteSection: String?
        get() = TUTORIAL_SCREEN_SITE_SECTION
}
