package org.cru.godtools.tutorial.analytics.model

import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.shared.analytics.TutorialAnalyticsAppSectionNames
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.PageSet

class TutorialAnalyticsScreenEvent internal constructor(
    private val tutorial: PageSet,
    private val page: Page?,
    pagePos: Int,
    locale: Locale?
) : AnalyticsScreenEvent("${tutorial.analyticsBaseScreenName}-${pagePos + 1}", locale) {
    override val appSection get() = when (tutorial) {
        PageSet.ONBOARDING -> TutorialAnalyticsAppSectionNames.ONBOARDING
        PageSet.FEATURES -> APP_SECTION_TUTORIAL
        // TODO: what should the app section actually be?
        PageSet.LIVE_SHARE -> null
        PageSet.TIPS -> null
    }

    override fun equals(other: Any?) = when {
        this === other -> true
        javaClass != other?.javaClass -> false
        !super.equals(other) -> false
        other !is TutorialAnalyticsScreenEvent -> false
        tutorial != other.tutorial -> false
        page != other.page -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + tutorial.hashCode()
        result = 31 * result + (page?.hashCode() ?: 0)
        return result
    }
}
