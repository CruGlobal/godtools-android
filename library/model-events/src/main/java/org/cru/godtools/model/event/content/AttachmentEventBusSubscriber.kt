package org.cru.godtools.model.event.content

import androidx.annotation.MainThread
import androidx.loader.content.Loader
import org.ccci.gto.android.common.eventbus.content.EventBusSubscriber
import org.cru.godtools.model.event.AttachmentUpdateEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class AttachmentEventBusSubscriber(loader: Loader<*>) : EventBusSubscriber(loader) {
    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAttachmentUpdateEvent(event: AttachmentUpdateEvent) = triggerLoad()
}
