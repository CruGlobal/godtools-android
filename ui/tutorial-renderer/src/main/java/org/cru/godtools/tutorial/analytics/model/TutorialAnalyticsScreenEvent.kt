package org.cru.godtools.tutorial.analytics.model

import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.PageSet

class TutorialAnalyticsScreenEvent internal constructor(
    private val tutorial: PageSet,
    private val page: Page?,
    pagePos: Int,
    locale: Locale?
) : AnalyticsScreenEvent("${tutorial.analyticsBaseScreenName}-${pagePos + 1}", locale) {
    override fun isForSystem(system: AnalyticsSystem) = when (page) {
        Page.ONBOARDING_WELCOME,
        Page.ONBOARDING_SHARE_FINAL,
        Page.ONBOARDING_LINKS -> system == AnalyticsSystem.APPSFLYER || super.isForSystem(system)
        else -> super.isForSystem(system)
    }

    override val appSection get() = when (tutorial) {
        PageSet.ONBOARDING -> APP_SECTION_ONBOARDING
        PageSet.FEATURES -> APP_SECTION_TUTORIAL
        // TODO: what should the app section actually be?
        PageSet.LIVE_SHARE -> null
        PageSet.TIPS -> null
    }
}
