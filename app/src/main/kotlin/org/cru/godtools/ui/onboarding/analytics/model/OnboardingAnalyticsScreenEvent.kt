package org.cru.godtools.ui.onboarding.analytics.model

import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.shared.analytics.TutorialAnalyticsAppSectionNames

class OnboardingAnalyticsScreenEvent(pageIndex: Int) : AnalyticsScreenEvent("onboarding-${pageIndex + 1}") {
    override val appSection get() = TutorialAnalyticsAppSectionNames.ONBOARDING
}
