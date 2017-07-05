package org.cru.godtools.tract.content;

import android.content.Context;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.cru.godtools.model.event.content.TranslationEventBusSubscriber;
import org.cru.godtools.tract.model.Manifest;
import org.cru.godtools.tract.service.TractManager;

import java.util.Locale;

public class TractManifestLoader extends CachingAsyncTaskEventBusLoader<Manifest> {
    private final TractManager mTractManager;

    private final long mToolId;
    @NonNull
    private final Locale mLocale;

    public TractManifestLoader(@NonNull final Context context, final long toolId, @NonNull final Locale locale) {
        super(context);
        mTractManager = TractManager.getInstance(context);
        mToolId = toolId;
        mLocale = locale;

        addEventBusSubscriber(new TranslationEventBusSubscriber(this));
    }

    @NonNull
    public Locale getLocale() {
        return mLocale;
    }

    @Override
    public Manifest loadInBackground() {
        try {
            return mTractManager.getLatestPublishedManifest(mToolId, mLocale).get();
        } catch (final Exception ignored) {
            return null;
        }
    }
}
