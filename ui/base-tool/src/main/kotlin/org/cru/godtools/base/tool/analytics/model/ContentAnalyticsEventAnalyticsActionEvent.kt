package org.cru.godtools.base.tool.analytics.model

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import io.fluidsonic.locale.toPlatform
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent
import org.cru.godtools.shared.tool.parser.model.Manifest

class ContentAnalyticsEventAnalyticsActionEvent(@get:VisibleForTesting val event: AnalyticsEvent, manifest: Manifest?) :
    AnalyticsActionEvent(action = event.action, locale = manifest?.locale?.toPlatform()) {
    override fun isForSystem(system: AnalyticsSystem) = when (system) {
        AnalyticsSystem.FACEBOOK -> event.isForSystem(AnalyticsEvent.System.FACEBOOK)
        AnalyticsSystem.FIREBASE -> event.isForSystem(AnalyticsEvent.System.FIREBASE)
        AnalyticsSystem.USER -> event.isForSystem(AnalyticsEvent.System.USER)
    }

    override val appSection = manifest?.code

    override val firebaseParams
        get() = Bundle().apply {
            event.attributes.forEach { putString(it.key, it.value) }
        }

    override val userCounterName = action
}
