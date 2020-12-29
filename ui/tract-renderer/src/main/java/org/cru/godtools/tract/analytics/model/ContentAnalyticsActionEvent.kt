package org.cru.godtools.tract.analytics.model

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.xml.model.AnalyticsEvent

class ContentAnalyticsActionEvent(@VisibleForTesting internal val event: AnalyticsEvent) :
    AnalyticsActionEvent(action = event.action.orEmpty()) {
    override fun isForSystem(system: AnalyticsSystem) = event.isForSystem(system)

    override val firebaseParams
        get() = Bundle().apply {
            when {
                event.isForSystem(AnalyticsSystem.FIREBASE) -> event.attributes.forEach { putString(it.key, it.value) }
            }
        }
}
