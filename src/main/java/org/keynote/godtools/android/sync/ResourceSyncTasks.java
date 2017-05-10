package org.keynote.godtools.android.sync;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SimpleArrayMap;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.ccci.gto.android.common.jsonapi.retrofit2.JsonApiParams;
import org.ccci.gto.android.common.jsonapi.util.Includes;
import org.keynote.godtools.android.model.Tool;
import org.keynote.godtools.android.model.Translation;

import java.io.IOException;

import retrofit2.Response;

import static org.ccci.gto.android.common.TimeConstants.DAY_IN_MS;

final class ResourceSyncTasks extends BaseDataSyncTasks {
    private static final Object LOCK_SYNC_RESOURCES = new Object();

    private static final String SYNC_TIME_RESOURCES = "last_synced.resources";
    private static final long STALE_DURATION_RESOURCES = DAY_IN_MS;

    private static final String INCLUDE_LATEST_TRANSLATIONS =
            Tool.JSON_LATEST_TRANSLATIONS + "." + Translation.JSON_LANGUAGE;

    ResourceSyncTasks(@NonNull final Context context) {
        super(context);
    }

    boolean syncResources(@NonNull final Bundle args) throws IOException {
        final SimpleArrayMap<Class<?>, Object> events = new SimpleArrayMap<>();

        synchronized (LOCK_SYNC_RESOURCES) {
            // short-circuit if we aren't forcing a sync and the data isn't stale
            final boolean force = isForced(args);
            if (!force &&
                    System.currentTimeMillis() - mDao.getLastSyncTime(SYNC_TIME_RESOURCES) < STALE_DURATION_RESOURCES) {
                return true;
            }

            // generate params & includes objects
            final Includes includes = new Includes(INCLUDE_LATEST_TRANSLATIONS);
            final JsonApiParams params = new JsonApiParams().include(INCLUDE_LATEST_TRANSLATIONS);

            // fetch resources from the API
            // short-circuit if this response is invalid
            final Response<JsonApiObject<Tool>> response = mApi.resources.list(params).execute();
            if (response == null || response.code() != 200) {
                return false;
            }

            // store fetched resources
            final JsonApiObject<Tool> json = response.body();
            if (json != null) {
                final LongSparseArray<Tool> existing = index(mDao.get(Query.select(Tool.class)));
                storeResources(events, json.getData(), existing, includes);
            }

            // send any pending events
            sendEvents(events);

            // update the sync time
            mDao.updateLastSyncTime(SYNC_TIME_RESOURCES);
        }

        return true;
    }
}
