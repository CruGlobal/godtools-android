package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import java.util.Locale

private const val TUTORIAL_SCREEN_SITE_SECTION = "tutorial"

class TutorialAnalyticsScreenEvent(screen: String) : AnalyticsScreenEvent(screen, Locale.getDefault()) {
    override val adobeSiteSection get() = TUTORIAL_SCREEN_SITE_SECTION
}
