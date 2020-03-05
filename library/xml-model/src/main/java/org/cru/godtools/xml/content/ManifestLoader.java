package org.cru.godtools.xml.content;

import android.content.Context;

import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.cru.godtools.model.loader.TranslationEventBusSubscriber;
import org.cru.godtools.xml.model.Manifest;
import org.cru.godtools.xml.service.ManifestManager;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

public class ManifestLoader extends CachingAsyncTaskEventBusLoader<Manifest> {
    private final ManifestManager mManifestManager;

    @NonNull
    private final String mTool;
    @NonNull
    private final Locale mLocale;

    public ManifestLoader(@NonNull final Context context, @NonNull final String toolCode,
                          @NonNull final Locale locale) {
        super(context);
        mManifestManager = ManifestManager.getInstance(context);
        mTool = toolCode;
        mLocale = locale;

        addEventBusSubscriber(new TranslationEventBusSubscriber(this));
    }

    @NonNull
    public Locale getLocale() {
        return mLocale;
    }

    @Override
    @WorkerThread
    public Manifest loadInBackground() {
        return mManifestManager.getLatestPublishedManifest(mTool, mLocale);
    }
}
