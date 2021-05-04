package org.cru.godtools.base.tool.service

import androidx.annotation.MainThread
import javax.inject.Inject
import javax.inject.Singleton
import org.cru.godtools.base.model.Event
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

@Singleton
class TimberContentEventLogger @Inject internal constructor(eventBus: EventBus) {
    init {
        eventBus.register(this)
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTrackContentEvent(event: Event) {
        Timber.tag("ContentEventLogger")
            .d("onContentEvent(%s:%s)", event.id.namespace, event.id.name)
    }
}
