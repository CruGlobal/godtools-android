package org.cru.godtools.tract.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.user.activity.UserCounterNames

private const val ACTION_SHARE_SCREEN_PUBLISHER = "share_screen_engaged"
private const val ACTION_SHARE_SCREEN_SUBSCRIBER = "share_screen_opened"

class ShareScreenEngagedActionEvent(tool: String?) : AnalyticsActionEvent(action = ACTION_SHARE_SCREEN_PUBLISHER) {
    override fun isForSystem(system: AnalyticsSystem) =
        system == AnalyticsSystem.FIREBASE || system == AnalyticsSystem.USER

    override val userCounterName = tool?.let { UserCounterNames.SCREEN_SHARE(it) }
}

object ShareScreenOpenedActionEvent : AnalyticsActionEvent(action = ACTION_SHARE_SCREEN_SUBSCRIBER) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.FIREBASE
}
