package org.keynote.godtools.android.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

public class GodToolsSyncService extends IntentService {
    public static final String EXTRA_SYNCTYPE = GodToolsSyncService.class.getName() + ".EXTRA_SYNCTYPE";

    // supported sync types
    static final int SYNCTYPE_NONE = 0;
    static final int SYNCTYPE_GROWTHSPACESSUBSCRIBERS = 1;

    public GodToolsSyncService() {
        super("GtSyncService");
    }

    public static void syncGrowthSpacesSubscribers(final Context context) {
        final Intent intent = new Intent(context, GodToolsSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_GROWTHSPACESSUBSCRIBERS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@NonNull final Intent intent) {
        switch (intent.getIntExtra(EXTRA_SYNCTYPE, SYNCTYPE_NONE)) {
            case SYNCTYPE_GROWTHSPACESSUBSCRIBERS:
                GrowthSpacesTasks.syncSubscribers(this);
                break;
        }
    }
}
