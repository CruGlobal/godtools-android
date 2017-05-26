package org.keynote.godtools.android.util;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.db.Query;
import org.cru.godtools.tract.service.TractManager;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.model.Translation;

import java.util.Locale;

public final class TractUtil {
    public static void preloadNewestPublishedTract(@NonNull final Context context, final long toolId,
                                                   @Nullable final Locale locale) {
        if (locale != null) {
            final TractManager tractManager = TractManager.getInstance(context);
            final GodToolsDao dao = GodToolsDao.getInstance(context);
            AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> dao.streamCompat(
                    Query.select(Translation.class)
                            .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(toolId, locale)
                                           .and(TranslationTable.SQL_WHERE_PUBLISHED)
                                           .and(TranslationTable.SQL_WHERE_DOWNLOADED))
                            .orderBy(TranslationTable.COLUMN_VERSION + " DESC")
                            .limit(1))
                    .map(Translation::getManifestFileName)
                    .withoutNulls()
                    .forEach(tractManager::getManifest));
        }
    }
}
