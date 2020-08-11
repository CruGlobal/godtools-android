package org.cru.godtools.analytics.model

import android.net.Uri
import java.util.Locale
import javax.annotation.concurrent.Immutable

private const val SNOWPLOW_CONTENT_SCORING_URI_PATH_ACTION = "action"

@Immutable
open class AnalyticsActionEvent(
    val action: String,
    val label: String? = null,
    locale: Locale? = null,
    systems: Collection<AnalyticsSystem>? = null
) : AnalyticsBaseEvent(locale, systems) {
    constructor(action: String, label: String? = null, locale: Locale? = null, system: AnalyticsSystem) :
        this(action, label, locale, setOf(system))

    open val adobeAttributes: Map<String, *>? get() = null

    open val firebaseEventName get() = action

    override val snowplowPageTitle = listOfNotNull(action, label).joinToString(" : ")
    override val snowplowContentScoringUri: Uri.Builder = super.snowplowContentScoringUri
        .authority(SNOWPLOW_CONTENT_SCORING_URI_PATH_ACTION)
        .appendPath(action)
}
