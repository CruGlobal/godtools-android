package org.cru.godtools.base.tool.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

private const val ACTION_SHARE = "share_icon_engaged"

object ShareActionEvent : AnalyticsActionEvent(action = ACTION_SHARE) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.FIREBASE
}
