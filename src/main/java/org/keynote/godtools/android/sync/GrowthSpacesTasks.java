package org.keynote.godtools.android.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.ccci.gto.android.common.db.Query;
import org.keynote.godtools.android.business.GSSubscriber;
import org.keynote.godtools.android.db.Contract.FollowupTable;
import org.keynote.godtools.android.model.Followup;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

import static org.ccci.gto.android.common.db.Expression.bind;

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
                final List<Followup> followups =
                        mDao.get(Query.select(Followup.class)
                                         .where(FollowupTable.FIELD_GS_ROUTE_ID.eq(bind(subscriber.getRouteId())))
                                         .limit(1));
                if (followups.size() > 0) {
                    final Followup followup = followups.get(0);
                    try {
                        final Response<GSSubscriber> response = mApi.growthSpaces
                                .createSubscriber(followup.getGrowthSpacesAccessId(),
                                                  followup.getGrowthSpacesAccessSecret(), subscriber).execute();
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
