package org.cru.godtools.tract.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

private const val ACTION_SHARE_SCREEN = "Share Screen Engaged"
private const val ADOBE_ATTR_SHARE_SCREEN = "cru.sharescreenengaged"

object ShareScreenActionEvent : AnalyticsActionEvent(action = ACTION_SHARE_SCREEN) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.ADOBE
    override val adobeAttributes = mapOf<String, Any>(ADOBE_ATTR_SHARE_SCREEN to 1)
}
