package org.cru.godtools.model.event.content

import androidx.annotation.MainThread
import androidx.loader.content.Loader
import org.ccci.gto.android.common.eventbus.content.EventBusSubscriber
import org.cru.godtools.model.event.LanguageUpdateEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class LanguageEventBusSubscriber(loader: Loader<*>) : EventBusSubscriber(loader) {
    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLanguageUpdateEvent(event: LanguageUpdateEvent) = triggerLoad()
}
