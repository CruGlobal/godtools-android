package org.cru.godtools.model.loader;

import org.ccci.gto.android.common.eventbus.content.EventBusSubscriber;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.loader.content.Loader;

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
