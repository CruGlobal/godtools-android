package org.cru.godtools.tract.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.xml.model.AnalyticsEvent

class ContentAnalyticsActionEvent(private val event: AnalyticsEvent) :
    AnalyticsActionEvent(null, event.action.orEmpty()) {
    override fun isForSystem(system: AnalyticsSystem) = event.isForSystem(system)
    override val adobeAttributes: Map<String?, *>? get() = event.attributes
}
