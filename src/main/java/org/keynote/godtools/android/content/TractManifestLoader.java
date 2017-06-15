package org.keynote.godtools.android.content;

import android.content.Context;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader;
import org.cru.godtools.model.event.content.TranslationEventBusSubscriber;
import org.cru.godtools.tract.model.Manifest;
import org.cru.godtools.tract.service.TractManager;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.model.Translation;

import java.util.Locale;

public class TractManifestLoader extends CachingAsyncTaskEventBusLoader<Manifest> {
    @NonNull
    private final GodToolsDao mDao;
    private final TractManager mTractManager;

    private final long mToolId;
    @NonNull
    private final Locale mLocale;

    private final Query<Translation> mQuery;

    public TractManifestLoader(@NonNull final Context context, final long toolId, @NonNull final Locale locale) {
        super(context);
        mDao = GodToolsDao.getInstance(context);
        mTractManager = TractManager.getInstance(context);
        mToolId = toolId;
        mLocale = locale;

        mQuery = Query.select(Translation.class)
                .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(mToolId, mLocale)
                               .and(TranslationTable.SQL_WHERE_PUBLISHED)
                               .and(TranslationTable.SQL_WHERE_DOWNLOADED))
                .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC)
                .limit(1);

        addEventBusSubscriber(new TranslationEventBusSubscriber(this));
    }

    @Override
    public Manifest loadInBackground() {
        // parse the manifest for the latest downloaded translation
        return mDao.streamCompat(mQuery)
                .findFirst()
                .map(Translation::getManifestFileName)
                .map(mTractManager::getManifest)
                .map(future -> {
                    try {
                        return future.get();
                    } catch (final Exception ignored) {
                    }

                    return null;
                })
                .orElse(null);
    }
}
