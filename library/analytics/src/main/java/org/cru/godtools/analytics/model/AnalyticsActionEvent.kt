package org.cru.godtools.analytics.model

import android.net.Uri
import java.util.Locale
import javax.annotation.concurrent.Immutable

private const val SNOWPLOW_CONTENT_SCORING_URI_PATH_ACTION = "action"

@Immutable
open class AnalyticsActionEvent @JvmOverloads constructor(
    val action: String,
    val label: String? = null,
    locale: Locale? = null
) : AnalyticsBaseEvent(locale) {
    open val adobeAttributes: Map<String?, *>? get() = null

    override val snowplowPageTitle = listOf(action, label).filterNot { it.isNullOrEmpty() }.joinToString(" : ")
    override val snowplowContentScoringUri: Uri.Builder = super.snowplowContentScoringUri
        .authority(SNOWPLOW_CONTENT_SCORING_URI_PATH_ACTION)
        .appendPath(action)
}
