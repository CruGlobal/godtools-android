package org.cru.godtools.model.event.content;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;

import org.ccci.gto.android.common.eventbus.content.EventBusSubscriber;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public final class TranslationEventBusSubscriber extends EventBusSubscriber {
    public TranslationEventBusSubscriber(@NonNull final Loader loader) {
        super(loader);
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTranslationUpdateEvent(@NonNull final TranslationUpdateEvent event) {
        triggerLoad();
    }
}
