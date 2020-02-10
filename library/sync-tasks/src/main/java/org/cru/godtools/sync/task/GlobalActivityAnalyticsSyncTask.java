package org.cru.godtools.sync.task;

import android.content.Context;
import android.os.Bundle;

import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.cru.godtools.model.GlobalActivityAnalytics;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import retrofit2.Response;

import static org.ccci.gto.android.common.base.TimeConstants.DAY_IN_MS;

public class GlobalActivityAnalyticsSyncTask extends BaseSyncTasks {

    private static final Object LOCK_SYNC_GLOBAL_ANALYTICS = new Object();

    private static final String SYNC_TIME_GLOBAL_ANALYTICS = "last_synced.global_analytics";
    private static final long STALE_DURATION_GLOBAL_ANALYTICS = DAY_IN_MS;

    public GlobalActivityAnalyticsSyncTask(@NonNull Context context) {
        super(context);
    }

    public boolean syncGlobalAnalytics(@NonNull final Bundle args) throws IOException {

        synchronized (LOCK_SYNC_GLOBAL_ANALYTICS) {
            final boolean force = isForced(args);
            if (!force && System.currentTimeMillis() - mDao.getLastSyncTime(SYNC_TIME_GLOBAL_ANALYTICS) <
                    STALE_DURATION_GLOBAL_ANALYTICS) {
                return true;
            }

            final Response<JsonApiObject<GlobalActivityAnalytics>> response =
                    mApi.getAnalytics().getGlobalActivity().execute();
            if (response == null || response.code() != 200) {
                return false;
            }

            final JsonApiObject<GlobalActivityAnalytics> json = response.body();
            if (json != null) {
                mDao.inTransaction(() -> {
                    storeGlobalAnalytics(json.getData());
                    return null;
                });
            }
            mDao.updateLastSyncTime(SYNC_TIME_GLOBAL_ANALYTICS);
        }
        return true;
    }

    private void storeGlobalAnalytics(List<GlobalActivityAnalytics> data) {
        for (final GlobalActivityAnalytics globalActivityAnalytics : data) {
            mDao.replace(globalActivityAnalytics);
        }
    }
}
