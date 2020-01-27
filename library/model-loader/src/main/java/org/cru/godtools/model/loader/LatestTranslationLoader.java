package org.cru.godtools.model.loader;

import android.content.Context;

import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.cru.godtools.model.Translation;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.Locale;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class LatestTranslationLoader extends CachingAsyncTaskEventBusLoader<Translation> {
    @NonNull
    private final GodToolsDao mDao;
    @NonNull
    private final String mTool;
    @Nullable
    private Locale mLocale;

    public LatestTranslationLoader(@NonNull final Context context, @NonNull final String toolCode,
                                   @Nullable final Locale locale) {
        super(context);
        mDao = GodToolsDao.Companion.getInstance(context);
        mTool = toolCode;
        mLocale = locale;
        addEventBusSubscriber(new TranslationEventBusSubscriber(this));
    }

    @MainThread
    public void setLocale(@Nullable final Locale locale) {
        mLocale = locale;
        onContentChanged();
    }

    @Nullable
    public Locale getLocale() {
        return mLocale;
    }

    @Override
    public Translation loadInBackground() {
        return mDao.getLatestTranslation(mTool, mLocale).orElse(null);
    }
}
