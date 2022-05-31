package org.cru.godtools.base.tool.analytics.model

import androidx.core.os.bundleOf
import org.cru.godtools.base.tool.model.Event

private const val ACTION_CONTENT_EVENT = "content_event"
private const val PARAM_EVENT_ID = "event_id"

internal class ContentEventAnalyticsActionEvent(private val event: Event) :
    ToolAnalyticsActionEvent(event.tool, ACTION_CONTENT_EVENT, locale = event.locale) {

    override val firebaseParams get() = bundleOf(PARAM_EVENT_ID to event.id.toString())
}
