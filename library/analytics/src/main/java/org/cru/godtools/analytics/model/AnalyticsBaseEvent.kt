package org.cru.godtools.analytics.model

import android.net.Uri
import java.util.Locale
import javax.annotation.concurrent.Immutable

private const val SNOWPLOW_CONTENT_SCORING_URI_SCHEME = "godtools"

@Immutable
abstract class AnalyticsBaseEvent internal constructor(
    val locale: Locale? = null,
    private val systems: Collection<AnalyticsSystem>? = null
) {
    /**
     * Return whether or not this Analytics event should be tracked in the specified service
     */
    open fun isForSystem(system: AnalyticsSystem) = systems == null || systems.contains(system)

    open val adobeSiteSection: String? get() = null
    open val adobeSiteSubSection: String? get() = null

    open val snowplowPageTitle: String? get() = null
    open val snowplowContentScoringUri: Uri.Builder get() =
        Uri.Builder()
            .scheme(SNOWPLOW_CONTENT_SCORING_URI_SCHEME)
}
