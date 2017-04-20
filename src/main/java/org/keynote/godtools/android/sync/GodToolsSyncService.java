package org.keynote.godtools.android.sync;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.ccci.gto.android.sync.ThreadedSyncIntentService;
import org.ccci.gto.android.sync.event.SyncFinishedEvent;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import static com.google.common.base.MoreObjects.firstNonNull;

public class GodToolsSyncService extends ThreadedSyncIntentService {
    public static final String EXTRA_SYNCTYPE = GodToolsSyncService.class.getName() + ".EXTRA_SYNCTYPE";

    // supported sync types
    static final int SYNCTYPE_NONE = 0;
    static final int SYNCTYPE_GROWTHSPACESSUBSCRIBERS = 1;
    static final int SYNCTYPE_LANGUAGES = 2;

    private GrowthSpacesTasks mGrowthSpacesTasks;
    private LanguagesSyncTasks mLanguagesSyncTasks;

    public static SyncTask syncLanguages(final Context context) {
        final Intent intent = new Intent(context, GodToolsSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_LANGUAGES);
        return new SyncTask(context, intent);
    }

    public static void syncGrowthSpacesSubscribers(final Context context) {
        final Intent intent = new Intent(context, GodToolsSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_GROWTHSPACESSUBSCRIBERS);
        context.startService(intent);
    }

    public GodToolsSyncService() {
        super("GtSyncService");
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate() {
        super.onCreate();
        mGrowthSpacesTasks = new GrowthSpacesTasks(this);
        mLanguagesSyncTasks = new LanguagesSyncTasks(this);
    }

    @Override
    protected void onHandleSyncIntent(@NonNull final Intent intent) {
        try {
            final Bundle args = firstNonNull(intent.getExtras(), Bundle.EMPTY);
            switch (intent.getIntExtra(EXTRA_SYNCTYPE, SYNCTYPE_NONE)) {
                case SYNCTYPE_GROWTHSPACESSUBSCRIBERS:
                    mGrowthSpacesTasks.syncSubscribers();
                    break;
                case SYNCTYPE_LANGUAGES:
                    mLanguagesSyncTasks.syncLanguages(args);
                    break;
            }
        } catch (final IOException ignored) {
        }
    }

    /* END lifecycle */

    protected void finishSync(final int syncId) {
        EventBus.getDefault().post(new SyncFinishedEvent(syncId));
    }
}
