package org.keynote.godtools.android.content;

import android.content.Context;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.event.content.LanguageEventBusSubscriber;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.List;

public final class LanguagesLoader extends CachingAsyncTaskEventBusLoader<List<Language>> {
    private static final Query<Language> QUERY_LANGUAGES = Query.select(Language.class);

    @NonNull
    private final GodToolsDao mDao;

    public LanguagesLoader(@NonNull final Context context) {
        super(context);
        mDao = GodToolsDao.getInstance(context);

        addEventBusSubscriber(new LanguageEventBusSubscriber(this));
    }

    @Override
    public List<Language> loadInBackground() {
        return mDao.get(QUERY_LANGUAGES);
    }
}
