package org.cru.godtools.model.loader;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.cru.godtools.model.Translation;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.Locale;

public final class LatestTranslationLoader extends CachingAsyncTaskEventBusLoader<Translation> {
    @NonNull
    private final GodToolsDao mDao;
    @NonNull
    private final String mTool;
    @NonNull
    private Locale mLocale;

    public LatestTranslationLoader(@NonNull final Context context, @NonNull final String toolCode,
                                   @NonNull final Locale locale) {
        super(context);
        mDao = GodToolsDao.getInstance(context);
        mTool = toolCode;
        mLocale = locale;
        addEventBusSubscriber(new TranslationEventBusSubscriber(this));
    }

    @MainThread
    public void setLocale(@NonNull final Locale locale) {
        mLocale = locale;
        onContentChanged();
    }

    @NonNull
    public Locale getLocale() {
        return mLocale;
    }

    @Override
    public Translation loadInBackground() {
        return mDao.getLatestTranslation(mTool, mLocale).orElse(null);
    }
}
