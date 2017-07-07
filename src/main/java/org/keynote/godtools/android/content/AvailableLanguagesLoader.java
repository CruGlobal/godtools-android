package org.keynote.godtools.android.content;

import android.content.Context;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.event.content.TranslationEventBusSubscriber;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.List;
import java.util.Locale;

public final class AvailableLanguagesLoader extends CachingAsyncTaskEventBusLoader<List<Locale>> {
    @NonNull
    private final GodToolsDao mDao;
    @NonNull
    private final String mTool;

    public AvailableLanguagesLoader(@NonNull final Context context, @NonNull final String toolCode) {
        super(context);
        mDao = GodToolsDao.getInstance(context);
        mTool = toolCode;
        addEventBusSubscriber(new TranslationEventBusSubscriber(this));
    }

    @Override
    public List<Locale> loadInBackground() {
        return mDao.streamCompat(Query.select(Translation.class).where(TranslationTable.FIELD_TOOL.eq(mTool)))
                .map(Translation::getLanguageCode)
                .withoutNulls()
                .distinct()
                .toList();
    }
}
