package org.cru.godtools.base.tool.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

private const val ACTION_SHARE = "Share Icon Engaged"
private const val ADOBE_SHARE_CONTENT = "cru.shareiconengaged"

object ShareActionEvent : AnalyticsActionEvent(action = ACTION_SHARE) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.ADOBE
    override val adobeAttributes = mapOf<String, Any>(ADOBE_SHARE_CONTENT to 1)
}
