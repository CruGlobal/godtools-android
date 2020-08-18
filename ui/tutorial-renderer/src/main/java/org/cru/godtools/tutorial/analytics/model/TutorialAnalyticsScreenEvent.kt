package org.cru.godtools.tutorial.analytics.model

import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.tutorial.PageSet

class TutorialAnalyticsScreenEvent(private val tutorial: PageSet, page: Int, locale: Locale?) :
    AnalyticsScreenEvent("${tutorial.analyticsBaseScreenName}-${page + 1}", locale) {
    override val adobeSiteSection get() = when (tutorial) {
        PageSet.ONBOARDING -> ADOBE_SITE_SECTION_ONBOARDING
        PageSet.TRAINING -> ADOBE_SITE_SECTION_TUTORIAL
        // TODO: what should the adobe site section actually be?
        PageSet.LIVE_SHARE -> null
    }
}
