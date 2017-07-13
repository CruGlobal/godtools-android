package org.cru.godtools.model.loader;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.cru.godtools.model.Translation;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.List;
import java.util.Locale;

public final class LatestTranslationLoader extends CachingAsyncTaskEventBusLoader<Translation> {
    private static final Query<Translation> QUERY = Query.select(Translation.class)
            .orderBy(TranslationTable.COLUMN_VERSION + " DESC")
            .limit(1);

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

    @Override
    public Translation loadInBackground() {
        final List<Translation> translations =
                mDao.get(QUERY.where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(mTool, mLocale)));
        return translations.isEmpty() ? null : translations.get(0);
    }
}
