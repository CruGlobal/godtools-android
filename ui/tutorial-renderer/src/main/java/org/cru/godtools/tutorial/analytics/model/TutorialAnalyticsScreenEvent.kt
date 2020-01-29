package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.tutorial.PageSet
import java.util.Locale

class TutorialAnalyticsScreenEvent(val tutorial: PageSet, page: Int) :
    AnalyticsScreenEvent("${tutorial.analyticsBaseScreenName}-${page + 1}", Locale.getDefault()) {
    override val adobeSiteSection
        get() = when (tutorial.analyticsBaseScreenName) {
            BASE_SCREEN_NAME_ON_BOARDING -> ADOBE_SITE_SECTION_ON_BOARDING
            else -> ADOBE_SITE_SECTION_TUTORIAL
        }
}
