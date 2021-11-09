package org.cru.godtools.base.tool.service

import javax.inject.Inject
import javax.inject.Singleton
import org.cru.godtools.base.tool.analytics.model.ContentEventAnalyticsActionEvent
import org.cru.godtools.base.tool.model.Event
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@Singleton
class ContentEventAnalyticsHandler @Inject constructor(private val eventBus: EventBus) {
    init {
        eventBus.register(this)
    }

    @Subscribe
    fun onContentEvent(event: Event) = eventBus.post(ContentEventAnalyticsActionEvent(event))
}
