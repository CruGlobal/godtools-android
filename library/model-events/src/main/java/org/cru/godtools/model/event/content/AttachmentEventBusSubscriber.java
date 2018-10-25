package org.cru.godtools.model.event.content;

import org.ccci.gto.android.common.eventbus.content.EventBusSubscriber;
import org.cru.godtools.model.event.AttachmentUpdateEvent;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.loader.content.Loader;

public final class AttachmentEventBusSubscriber extends EventBusSubscriber {
    public AttachmentEventBusSubscriber(@NonNull final Loader loader) {
        super(loader);
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAttachmentUpdateEvent(@NonNull final AttachmentUpdateEvent event) {
        triggerLoad();
    }
}
