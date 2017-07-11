package org.cru.godtools.sync;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.ccci.gto.android.sync.ThreadedSyncIntentService;
import org.ccci.gto.android.sync.event.SyncFinishedEvent;
import org.cru.godtools.sync.job.SyncJobCreator;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import static android.content.ContentResolver.SYNC_EXTRAS_MANUAL;
import static com.google.common.base.MoreObjects.firstNonNull;

public class GodToolsSyncService extends ThreadedSyncIntentService {
    public static final String EXTRA_SYNCTYPE = GodToolsSyncService.class.getName() + ".EXTRA_SYNCTYPE";

    // supported sync types
    static final int SYNCTYPE_NONE = 0;
    static final int SYNCTYPE_LANGUAGES = 2;
    static final int SYNCTYPE_TOOLS = 3;
    static final int SYNCTYPE_FOLLOWUPS = 4;
    static final int SYNCTYPE_TOOL_SHARES = 5;

    private LanguagesSyncTasks mLanguagesSyncTasks;
    private ToolSyncTasks mToolSyncTasks;
    private FollowupSyncTasks mFollowupSyncTasks;

    public static SyncTask syncLanguages(final Context context, final boolean force) {
        final Intent intent = new Intent(context, GodToolsSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_LANGUAGES);
        intent.putExtra(SYNC_EXTRAS_MANUAL, force);
        return new SyncTask(context, intent);
    }

    public static SyncTask syncTools(final Context context, final boolean force) {
        final Intent intent = new Intent(context, GodToolsSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_TOOLS);
        intent.putExtra(SYNC_EXTRAS_MANUAL, force);
        return new SyncTask(context, intent);
    }

    @NonNull
    public static SyncTask syncFollowups(final Context context) {
        final Intent intent = new Intent(context, GodToolsSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_FOLLOWUPS);
        return new SyncTask(context, intent);
    }

    @NonNull
    public static SyncTask syncToolShares(@NonNull final Context context) {
        final Intent intent = new Intent(context, GodToolsSyncService.class);
        intent.putExtra(EXTRA_SYNCTYPE, SYNCTYPE_TOOL_SHARES);
        return new SyncTask(context, intent);
    }

    public GodToolsSyncService() {
        super("GtSyncService");
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate() {
        super.onCreate();
        mLanguagesSyncTasks = new LanguagesSyncTasks(this);
        mToolSyncTasks = new ToolSyncTasks(this);
        mFollowupSyncTasks = new FollowupSyncTasks(this);
    }

    @Override
    protected void onHandleSyncIntent(@NonNull final Intent intent) {
        try {
            final Bundle args = firstNonNull(intent.getExtras(), Bundle.EMPTY);
            switch (intent.getIntExtra(EXTRA_SYNCTYPE, SYNCTYPE_NONE)) {
                case SYNCTYPE_LANGUAGES:
                    mLanguagesSyncTasks.syncLanguages(args);
                    break;
                case SYNCTYPE_TOOLS:
                    mToolSyncTasks.syncResources(args);
                    break;
                case SYNCTYPE_FOLLOWUPS:
                    try {
                        mFollowupSyncTasks.syncFollowups();
                    } catch (final IOException e) {
                        SyncJobCreator.SyncFollowupJob.scheduleJob();
                        throw e;
                    }
                    break;
                case SYNCTYPE_TOOL_SHARES:
                    final boolean result = mToolSyncTasks.syncShares();
                    if (!result) {
                        SyncJobCreator.SyncSharesJob.scheduleJob();
                    }
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
