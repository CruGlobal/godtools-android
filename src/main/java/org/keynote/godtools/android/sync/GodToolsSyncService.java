package org.keynote.godtools.android.sync;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.ccci.gto.android.sync.ThreadedSyncIntentService;
import org.ccci.gto.android.sync.event.SyncFinishedEvent;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static com.google.common.base.MoreObjects.firstNonNull;

public class GodToolsSyncService extends ThreadedSyncIntentService {
    public static final String EXTRA_SYNCTYPE = GodToolsSyncService.class.getName() + ".EXTRA_SYNCTYPE";

    // supported sync types
    static final int SYNCTYPE_NONE = 0;
    static final int SYNCTYPE_GROWTHSPACESSUBSCRIBERS = 1;
    static final int SYNCTYPE_LANGUAGES = 2;
    static final int SYNCTYPE_RESOURCES = 3;

    private GrowthSpacesTasks mGrowthSpacesTasks;
    private LanguagesSyncTasks mLanguagesSyncTasks;
    private ResourceSyncTasks mResourceSyncTasks;

    public static SyncTask syncLanguages(final Context context, final boolean force) {
        final Intent intent = new Intent(context, GodToolsSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_LANGUAGES);
        intent.putExtra(SYNC_EXTRAS_MANUAL, force);
        return new SyncTask(context, intent);
    }

    public static SyncTask syncResources(final Context context, final boolean force) {
        final Intent intent = new Intent(context, GodToolsSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_RESOURCES);
        intent.putExtra(SYNC_EXTRAS_MANUAL, force);
        return new SyncTask(context, intent);
    }

    public static SyncTask syncGrowthSpacesSubscribers(final Context context) {
        final Intent intent = new Intent(context, GodToolsSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_GROWTHSPACESSUBSCRIBERS);
        return new SyncTask(context, intent);
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
        mResourceSyncTasks = new ResourceSyncTasks(this);
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
                case SYNCTYPE_RESOURCES:
                    mResourceSyncTasks.syncResources(args);
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
