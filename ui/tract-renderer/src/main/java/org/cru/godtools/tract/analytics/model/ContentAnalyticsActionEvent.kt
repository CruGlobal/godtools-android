package org.cru.godtools.tract.analytics.model

import androidx.annotation.VisibleForTesting
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.xml.model.AnalyticsEvent

class ContentAnalyticsActionEvent(@VisibleForTesting internal val event: AnalyticsEvent) :
    AnalyticsActionEvent(action = event.action.orEmpty()) {
    override fun isForSystem(system: AnalyticsSystem) = event.isForSystem(system)
    override val adobeAttributes: Map<String, *>? get() = event.attributes
    override val firebaseEventName = if (isForSystem(AnalyticsSystem.ADOBE)) {
        event.action?.replace(Regex("[ \\-]"), "_").orEmpty().toLowerCase()
    } else {
        super.firebaseEventName.toLowerCase()
    }
}
