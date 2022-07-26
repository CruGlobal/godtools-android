package org.cru.godtools.analytics.model

import android.net.Uri
import android.os.Bundle
import java.util.Locale
import javax.annotation.concurrent.Immutable

private const val SNOWPLOW_CONTENT_SCORING_URI_PATH_ACTION = "action"

@Immutable
open class AnalyticsActionEvent(
    val action: String,
    val label: String? = null,
    locale: Locale? = null,
    systems: Collection<AnalyticsSystem> = DEFAULT_SYSTEMS
) : AnalyticsBaseEvent(locale, systems) {
    constructor(action: String, label: String? = null, locale: Locale? = null, system: AnalyticsSystem) :
        this(action, label, locale, setOf(system))

    open val firebaseEventName get() = action
    open val firebaseParams get() = Bundle()

    open val snowplowCategory = "action"
    override val snowplowPageTitle = listOfNotNull(action, label).joinToString(" : ")
    override val snowplowContentScoringUri: Uri.Builder get() = super.snowplowContentScoringUri
        .authority(SNOWPLOW_CONTENT_SCORING_URI_PATH_ACTION)
        .appendPath(action)

    override val userCounterName get() = action
}
