package org.cru.godtools.tract.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

private const val ACTION_SHARE_SCREEN_PUBLISHER = "Share Screen Engaged"
private const val ADOBE_ATTR_SHARE_SCREEN_PUBLISHER = "cru.sharescreenengaged"

private const val ACTION_SHARE_SCREEN_SUBSCRIBER = "Share Screen Opened"
private const val ADOBE_ATTR_SHARE_SCREEN_SUBSCRIBER = "cru.share_screen_open"

object ShareScreenEngagedActionEvent : AnalyticsActionEvent(action = ACTION_SHARE_SCREEN_PUBLISHER) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.ADOBE
    override val adobeAttributes = mapOf(ADOBE_ATTR_SHARE_SCREEN_PUBLISHER to 1)
    override val firebaseEventName = "share_screen_engaged"
}

object ShareScreenOpenedActionEvent : AnalyticsActionEvent(action = ACTION_SHARE_SCREEN_SUBSCRIBER) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.ADOBE
    override val adobeAttributes = mapOf(ADOBE_ATTR_SHARE_SCREEN_SUBSCRIBER to 1)
    override val firebaseEventName = "share_screen_opened"
}
