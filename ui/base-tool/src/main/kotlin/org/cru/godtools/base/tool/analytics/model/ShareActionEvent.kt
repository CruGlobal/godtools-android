package org.cru.godtools.base.tool.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.ACTION_SHARE
import org.cru.godtools.shared.user.activity.UserCounterNames

object ShareActionEvent : AnalyticsActionEvent(action = ACTION_SHARE) {
    override fun isForSystem(system: AnalyticsSystem) = when (system) {
        AnalyticsSystem.FIREBASE, AnalyticsSystem.USER -> true
        else -> false
    }

    override val userCounterName = UserCounterNames.LINK_SHARED
}
