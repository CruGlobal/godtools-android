package org.cru.godtools.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.ccci.gto.android.common.db.Query;
import org.keynote.godtools.android.business.GSSubscriber;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

@Deprecated
@WorkerThread
class GrowthSpacesTasks extends BaseSyncTasks {
    private static final Object LOCK_SUBSCRIBERS = new Object();

    GrowthSpacesTasks(@NonNull final Context context) {
        super(context);
    }

    void syncSubscribers() {
        synchronized (LOCK_SUBSCRIBERS) {
            // fetch any pending subscribers
            final List<GSSubscriber> subscribers = mDao.get(Query.select(GSSubscriber.class));
            for (final GSSubscriber subscriber : subscribers) {
                // fetch the followup record for this subscriber (to retrieve access-id and access-secret)
                if (true) {
                    try {
                        final Response<GSSubscriber> response = mApi.growthSpaces
                                .createSubscriber("", "", subscriber).execute();
                        if (response.isSuccessful()) {
                            mDao.delete(subscriber);
                        }
                    } catch (final IOException ignored) {
                    }
                }
            }
        }
    }
}
