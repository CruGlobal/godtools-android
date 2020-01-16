package org.cru.godtools.analytics.model

import android.net.Uri
import java.util.Locale
import javax.annotation.concurrent.Immutable

private const val SNOWPLOW_CONTENT_SCORING_URI_PATH_ACTION = "action"

@Immutable
open class AnalyticsActionEvent @JvmOverloads constructor(
    val category: String?,
    val action: String,
    val label: String? = null,
    locale: Locale? = null
) : AnalyticsBaseEvent(locale) {
    open val attributes: Map<String?, *>? get() = null

    override val snowPlowPageTitle =
        listOf(category, action, label).filterNot { it.isNullOrEmpty() }.joinToString(" : ")
    override val snowPlowContentScoringUri: Uri.Builder = super.snowPlowContentScoringUri
        .authority(SNOWPLOW_CONTENT_SCORING_URI_PATH_ACTION)
        .appendPath(category)
        .appendPath(action)
}
