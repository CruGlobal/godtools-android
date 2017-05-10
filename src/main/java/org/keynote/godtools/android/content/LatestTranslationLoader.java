package org.keynote.godtools.android.content;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.model.Translation;

import java.util.List;
import java.util.Locale;

public final class LatestTranslationLoader extends CachingAsyncTaskEventBusLoader<Translation> {
    private static final Query<Translation> QUERY = Query.select(Translation.class)
            .orderBy(TranslationTable.COLUMN_VERSION + " DESC")
            .limit(1);

    @NonNull
    private final GodToolsDao mDao;
    private final long mResourceId;
    @NonNull
    private Locale mLocale;

    public LatestTranslationLoader(@NonNull final Context context, final long resourceId,
                                   @NonNull final Locale locale) {
        super(context);
        mDao = GodToolsDao.getInstance(context);
        mResourceId = resourceId;
        mLocale = locale;
        addEventBusSubscriber(new TranslationEventBusSubscriber(this));
    }

    @MainThread
    public void setLocale(@NonNull final Locale locale) {
        mLocale = locale;
        onContentChanged();
    }

    @Override
    public Translation loadInBackground() {
        final List<Translation> translations =
                mDao.get(QUERY.where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(mResourceId, mLocale)));
        return translations.isEmpty() ? null : translations.get(0);
    }
}
