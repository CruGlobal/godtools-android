package org.keynote.godtools.android.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.ccci.gto.android.common.db.Query;
import org.keynote.godtools.android.api.GrowthSpacesApi;
import org.keynote.godtools.android.business.GSSubscriber;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.dao.DBContract.FollowupTable;
import org.keynote.godtools.android.model.Followup;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

import static org.ccci.gto.android.common.db.Expression.bind;

@WorkerThread
class GrowthSpacesTasks {
    static synchronized void syncSubscribers(@NonNull final Context context) {
        final GrowthSpacesApi api = GrowthSpacesApi.INSTANCE;
        final DBAdapter dao = DBAdapter.getInstance(context);

        // fetch any pending subscribers
        final List<GSSubscriber> subscribers = dao.get(Query.select(GSSubscriber.class));
        for (final GSSubscriber subscriber : subscribers) {
            // fetch the followup record for this subscriber (to retrieve access-id and access-secret)
            final List<Followup> followups =
                    dao.get(Query.select(Followup.class)
                                    .where(FollowupTable.FIELD_GS_ROUTE_ID.eq(bind(subscriber.getRouteId()))).limit(1));
            if (followups.size() > 0) {
                final Followup followup = followups.get(0);
                try {
                    final Response<GSSubscriber> response =
                            api.createSubscriber(followup.getGrowthSpacesAccessId(),
                                                 followup.getGrowthSpacesAccessSecret(), subscriber).execute();
                    if (response.isSuccessful()) {
                        dao.delete(subscriber);
                    }
                } catch (final IOException ignored) {
                }
            }
        }
    }
}
