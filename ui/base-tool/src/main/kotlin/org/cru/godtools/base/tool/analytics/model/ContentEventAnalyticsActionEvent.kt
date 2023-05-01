package org.cru.godtools.base.tool.analytics.model

import androidx.core.os.bundleOf
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.ACTION_CONTENT_EVENT
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsActionNames.PARAM_CONTENT_EVENT_ID

internal class ContentEventAnalyticsActionEvent(private val event: Event) :
    ToolAnalyticsActionEvent(event.tool, ACTION_CONTENT_EVENT, locale = event.locale) {

    override val firebaseParams get() = bundleOf(PARAM_CONTENT_EVENT_ID to event.id.toString())
}
