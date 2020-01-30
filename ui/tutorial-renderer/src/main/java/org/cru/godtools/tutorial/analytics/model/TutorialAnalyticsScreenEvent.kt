package org.cru.godtools.tutorial.analytics.model

import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.tutorial.PageSet
import java.util.Locale

class TutorialAnalyticsScreenEvent(private val tutorial: PageSet, page: Int) :
    AnalyticsScreenEvent("${tutorial.analyticsBaseScreenName}-${page + 1}", Locale.getDefault()) {
    override val adobeSiteSection get() = when (tutorial) {
        PageSet.ONBOARDING -> ADOBE_SITE_SECTION_ONBOARDING
        PageSet.TRAINING -> ADOBE_SITE_SECTION_TUTORIAL
    }
}
