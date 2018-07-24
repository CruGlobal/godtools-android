package org.cru.godtools.sync.task;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

import com.annimon.stream.Collectors;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.db.Transaction;
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams;
import org.cru.godtools.model.Language;
import org.keynote.godtools.android.db.Contract.LanguageTable;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import retrofit2.Response;
import timber.log.Timber;

import static org.ccci.gto.android.common.TimeConstants.WEEK_IN_MS;

public final class LanguagesSyncTasks extends BaseDataSyncTasks {
    private static final Object LOCK_SYNC_LANGUAGES = new Object();

    private static final String SYNC_TIME_LANGUAGES = "last_synced.languages";
    private static final long STALE_DURATION_LANGUAGES = WEEK_IN_MS;

    public LanguagesSyncTasks(@NonNull final Context context) {
        super(context);
    }

    public boolean syncLanguages(@NonNull final Bundle args) throws IOException {
        final SimpleArrayMap<Class<?>, Object> events = new SimpleArrayMap<>();

        synchronized (LOCK_SYNC_LANGUAGES) {
            // short-circuit if we aren't forcing a sync and the data isn't stale
            final boolean force = isForced(args);
            if (!force &&
                    System.currentTimeMillis() - mDao.getLastSyncTime(SYNC_TIME_LANGUAGES) < STALE_DURATION_LANGUAGES) {
                return true;
            }

            // fetch languages from the API
            // short-circuit if this response is invalid
            final Response<JsonApiObject<Language>> response = mApi.languages.list(new JsonApiParams()).execute();
            if (response == null || response.code() != 200) {
                return false;
            }

            // store languages
            final JsonApiObject<Language> json = response.body();
            if (json != null) {
                final Transaction tx = mDao.newTransaction();
                try {
                    tx.beginTransaction();
                    final Map<Locale, Language> existing = mDao.streamCompat(Query.select(Language.class))
                            .collect(Collectors.toMap(Language::getCode, l -> l, (l1, l2) -> {
                                Timber.tag("LanguagesSyncTask")
                                        .d(new RuntimeException("Duplicate Language sync error"),
                                           "Duplicate languages detected: %s %s", l1, l2);
                                mDao.delete(Language.class,
                                            LanguageTable.FIELD_ID.in(Expression.constants(l1.getId(), l2.getId())));
                                return l1;
                            }));
                    storeLanguages(events, json.getData(), existing);

                    tx.setTransactionSuccessful();
                } finally {
                    tx.endTransaction().recycle();
                }

                // send any pending events
                sendEvents(events);

                // update the sync time
                mDao.updateLastSyncTime(SYNC_TIME_LANGUAGES);
            }
        }

        return true;
    }
}
