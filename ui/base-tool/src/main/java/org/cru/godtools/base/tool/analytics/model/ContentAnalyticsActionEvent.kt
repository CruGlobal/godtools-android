package org.cru.godtools.base.tool.analytics.model

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.tool.model.AnalyticsEvent

class ContentAnalyticsActionEvent(@VisibleForTesting val event: AnalyticsEvent) :
    AnalyticsActionEvent(action = event.action.orEmpty()) {
    override fun isForSystem(system: AnalyticsSystem) = event.isForSystem(
        when (system) {
            AnalyticsSystem.ADOBE -> AnalyticsEvent.System.ADOBE
            AnalyticsSystem.APPSFLYER -> AnalyticsEvent.System.APPSFLYER
            AnalyticsSystem.FACEBOOK -> AnalyticsEvent.System.FACEBOOK
            AnalyticsSystem.FIREBASE -> AnalyticsEvent.System.FIREBASE
            AnalyticsSystem.SNOWPLOW -> AnalyticsEvent.System.SNOWPLOW
        }
    )

    override val appSection get() = event.manifest.code

    override val adobeAttributes get() = event.attributes

    override val firebaseEventName get() = when {
        isForSystem(AnalyticsSystem.ADOBE) -> event.action?.sanitizeAdobeNameForFirebase().orEmpty()
        else -> super.firebaseEventName
    }

    override val firebaseParams
        get() = Bundle().apply {
            when {
                event.isForSystem(AnalyticsEvent.System.FIREBASE) ->
                    event.attributes.forEach { putString(it.key, it.value) }
                event.isForSystem(AnalyticsEvent.System.ADOBE) ->
                    event.attributes.forEach { putString(it.key.sanitizeAdobeNameForFirebase(), it.value) }
            }
        }
}
