package org.cru.godtools.tract.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

private const val ACTION_SHARE_SCREEN_PUBLISHER = "share_screen_engaged"

private const val ACTION_SHARE_SCREEN_SUBSCRIBER = "share_screen_opened"

object ShareScreenEngagedActionEvent : AnalyticsActionEvent(action = ACTION_SHARE_SCREEN_PUBLISHER) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.FIREBASE
}

object ShareScreenOpenedActionEvent : AnalyticsActionEvent(action = ACTION_SHARE_SCREEN_SUBSCRIBER) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.FIREBASE
}
