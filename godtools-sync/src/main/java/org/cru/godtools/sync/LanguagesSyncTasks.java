package org.cru.godtools.sync;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;

import com.annimon.stream.Collectors;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams;
import org.cru.godtools.model.Language;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import retrofit2.Response;

import static org.ccci.gto.android.common.TimeConstants.WEEK_IN_MS;

final class LanguagesSyncTasks extends BaseDataSyncTasks {
    private static final Object LOCK_SYNC_LANGUAGES = new Object();

    private static final String SYNC_TIME_LANGUAGES = "last_synced.languages";
    private static final long STALE_DURATION_LANGUAGES = WEEK_IN_MS;

    LanguagesSyncTasks(@NonNull final Context context) {
        super(context);
    }

    boolean syncLanguages(@NonNull final Bundle args) throws IOException {
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
                final Map<Locale, Language> existing = mDao.streamCompat(Query.select(Language.class))
                        .collect(Collectors.toMap(Language::getCode, l -> l));
                storeLanguages(events, json.getData(), existing);
            }

            // send any pending events
            sendEvents(events);

            // update the sync time
            mDao.updateLastSyncTime(SYNC_TIME_LANGUAGES);
        }

        return true;
    }
}
