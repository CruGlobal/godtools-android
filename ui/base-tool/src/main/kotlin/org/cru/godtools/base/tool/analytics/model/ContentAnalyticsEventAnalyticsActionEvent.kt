package org.cru.godtools.base.tool.analytics.model

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent

class ContentAnalyticsEventAnalyticsActionEvent(@get:VisibleForTesting val event: AnalyticsEvent) :
    AnalyticsActionEvent(action = event.action, locale = event.manifest.locale) {
    override fun isForSystem(system: AnalyticsSystem) = when (system) {
        AnalyticsSystem.FACEBOOK -> event.isForSystem(AnalyticsEvent.System.FACEBOOK)
        AnalyticsSystem.FIREBASE -> event.isForSystem(AnalyticsEvent.System.FIREBASE)
        AnalyticsSystem.USER -> event.isForSystem(AnalyticsEvent.System.USER)
    }

    override val appSection get() = event.manifest.code

    override val firebaseParams
        get() = Bundle().apply {
            event.attributes.forEach { putString(it.key, it.value) }
        }

    override val userCounterName = action
}
