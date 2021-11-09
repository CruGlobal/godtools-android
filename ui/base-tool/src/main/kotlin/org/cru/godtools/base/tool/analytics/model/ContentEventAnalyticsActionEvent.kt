package org.cru.godtools.base.tool.analytics.model

import android.os.Bundle
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.base.tool.model.Event

private const val ACTION_CONTENT_EVENT = "content_event"
private const val PARAM_EVENT_ID = "event_id"

internal class ContentEventAnalyticsActionEvent(private val event: Event) :
    AnalyticsActionEvent(ACTION_CONTENT_EVENT, locale = event.locale) {

    override val appSection get() = event.tool

    override val firebaseParams get() = Bundle().apply {
        putString(PARAM_EVENT_ID, event.id.toString())
    }
}
