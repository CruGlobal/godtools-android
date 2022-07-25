package org.cru.godtools.base.tool.analytics.model

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.tool.model.AnalyticsEvent

class ContentAnalyticsEventAnalyticsActionEvent(@get:VisibleForTesting val event: AnalyticsEvent) :
    AnalyticsActionEvent(action = event.action.orEmpty(), locale = event.manifest.locale) {
    override fun isForSystem(system: AnalyticsSystem) = when (system) {
        AnalyticsSystem.APPSFLYER -> event.isForSystem(AnalyticsEvent.System.APPSFLYER)
        AnalyticsSystem.FACEBOOK -> event.isForSystem(AnalyticsEvent.System.FACEBOOK)
        AnalyticsSystem.FIREBASE ->
            event.isForSystem(AnalyticsEvent.System.ADOBE) || event.isForSystem(AnalyticsEvent.System.FIREBASE)
        AnalyticsSystem.SNOWPLOW -> event.isForSystem(AnalyticsEvent.System.SNOWPLOW)
        AnalyticsSystem.USER -> event.isForSystem(AnalyticsEvent.System.USER)
    }

    override val appSection get() = event.manifest.code

    override val firebaseEventName get() = when {
        event.isForSystem(AnalyticsEvent.System.FIREBASE) -> super.firebaseEventName
        event.isForSystem(AnalyticsEvent.System.ADOBE) -> event.action?.sanitizeAdobeNameForFirebase().orEmpty()
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

@VisibleForTesting
internal fun String.sanitizeAdobeNameForFirebase(): String = replace(Regex("[ \\-.]"), "_").lowercase(Locale.ROOT)
