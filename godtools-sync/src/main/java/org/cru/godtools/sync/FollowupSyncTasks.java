package org.cru.godtools.sync;

import android.content.Context;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject;
import org.cru.godtools.model.Followup;
import org.keynote.godtools.android.model.Language;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;

final class FollowupSyncTasks extends BaseSyncTasks {
    private static final Object LOCK_FOLLOWUPS = new Object();

    FollowupSyncTasks(@NonNull final Context context) {
        super(context);
    }

    void syncFollowups() throws IOException {
        synchronized (LOCK_FOLLOWUPS) {
            final List<Followup> followups = mDao.get(Query.select(Followup.class));
            for (final Followup followup : followups) {
                final Language language = mDao.find(Language.class, followup.getLanguageCode());
                followup.setLanguage(language);
                followup.stashId();

                final Response<JsonApiObject<Followup>> response = mApi.followups.subscribe(followup).execute();
                if (response.code() == 204) {
                    followup.restoreId();
                    mDao.delete(followup);
                }
            }
        }
    }
}
