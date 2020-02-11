package org.cru.godtools.sync

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.ccci.gto.android.sync.ThreadedSyncIntentService
import org.ccci.gto.android.sync.event.SyncFinishedEvent
import org.cru.godtools.sync.task.FollowupSyncTasks
import org.cru.godtools.sync.task.LanguagesSyncTasks
import org.cru.godtools.sync.task.ToolSyncTasks
import org.cru.godtools.sync.work.scheduleSyncFollowupWork
import org.cru.godtools.sync.work.scheduleSyncToolSharesWork
import org.greenrobot.eventbus.EventBus
import java.io.IOException

// supported sync types
private const val EXTRA_SYNCTYPE = "org.cru.godtools.sync.GodToolsSyncService.EXTRA_SYNCTYPE"
private const val SYNCTYPE_NONE = 0
private const val SYNCTYPE_LANGUAGES = 2
private const val SYNCTYPE_TOOLS = 3
private const val SYNCTYPE_FOLLOWUPS = 4
private const val SYNCTYPE_TOOL_SHARES = 5

private fun Intent.toSyncTask(context: Context): ThreadedSyncIntentService.SyncTask {
    return ThreadedSyncIntentService.SyncTask(context, this)
}

fun syncLanguages(context: Context, force: Boolean): ThreadedSyncIntentService.SyncTask {
    return Intent(context, GodToolsSyncService::class.java)
            .putExtra(EXTRA_SYNCTYPE, SYNCTYPE_LANGUAGES)
            .putExtra(ContentResolver.SYNC_EXTRAS_MANUAL, force)
            .toSyncTask(context)
}

fun syncTools(context: Context, force: Boolean): ThreadedSyncIntentService.SyncTask {
    return Intent(context, GodToolsSyncService::class.java)
            .putExtra(EXTRA_SYNCTYPE, SYNCTYPE_TOOLS)
            .putExtra(ContentResolver.SYNC_EXTRAS_MANUAL, force)
            .toSyncTask(context)
}

fun syncFollowups(context: Context): ThreadedSyncIntentService.SyncTask {
    return Intent(context, GodToolsSyncService::class.java)
            .putExtra(EXTRA_SYNCTYPE, SYNCTYPE_FOLLOWUPS)
            .toSyncTask(context)
}

fun syncToolShares(context: Context): ThreadedSyncIntentService.SyncTask {
    return Intent(context, GodToolsSyncService::class.java)
            .putExtra(EXTRA_SYNCTYPE, SYNCTYPE_TOOL_SHARES)
            .toSyncTask(context)
}

class GodToolsSyncService : ThreadedSyncIntentService("GtSyncService") {
    private lateinit var mLanguagesSyncTasks: LanguagesSyncTasks
    private lateinit var mToolSyncTasks: ToolSyncTasks
    private lateinit var mFollowupSyncTasks: FollowupSyncTasks

    // region Lifecycle Events

    override fun onCreate() {
        super.onCreate()
        mLanguagesSyncTasks = LanguagesSyncTasks.getInstance(this)
        mToolSyncTasks = ToolSyncTasks(this)
        mFollowupSyncTasks = FollowupSyncTasks.getInstance(this)
    }

    override fun onHandleSyncIntent(intent: Intent) {
        try {
            val args = intent.extras ?: Bundle.EMPTY
            when (intent.getIntExtra(EXTRA_SYNCTYPE, SYNCTYPE_NONE)) {
                SYNCTYPE_LANGUAGES -> mLanguagesSyncTasks.syncLanguages(args)
                SYNCTYPE_TOOLS -> mToolSyncTasks.syncTools(args)
                SYNCTYPE_FOLLOWUPS -> try {
                    mFollowupSyncTasks.syncFollowups()
                } catch (e: IOException) {
                    scheduleSyncFollowupWork()
                    throw e
                }
                SYNCTYPE_TOOL_SHARES -> {
                    val result = mToolSyncTasks.syncShares()
                    if (!result) scheduleSyncToolSharesWork()
                }
            }
        } catch (ignored: IOException) {
        }
    }

    // endregion Lifecycle Events

    override fun finishSync(syncId: Int) {
        EventBus.getDefault().post(SyncFinishedEvent(syncId))
    }
}
