package org.cru.godtools.sync

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.ccci.gto.android.common.sync.SyncTask
import org.ccci.gto.android.common.sync.event.SyncFinishedEvent
import org.ccci.gto.android.sync.ThreadedSyncIntentService
import org.cru.godtools.sync.task.FollowupSyncTasks
import org.cru.godtools.sync.task.LanguagesSyncTasks
import org.cru.godtools.sync.work.scheduleSyncFollowupWork
import org.greenrobot.eventbus.EventBus
import java.io.IOException

// supported sync types
internal const val EXTRA_SYNCTYPE = "org.cru.godtools.sync.GodToolsSyncService.EXTRA_SYNCTYPE"
private const val SYNCTYPE_NONE = 0
private const val SYNCTYPE_FOLLOWUPS = 4

private fun Intent.toSyncTask(context: Context): SyncTask {
    return ThreadedSyncIntentService.SyncTask(context, this)
}

fun syncFollowups(context: Context): SyncTask {
    return Intent(context, GodToolsSyncService::class.java)
            .putExtra(EXTRA_SYNCTYPE, SYNCTYPE_FOLLOWUPS)
            .toSyncTask(context)
}

class GodToolsSyncService : ThreadedSyncIntentService("GtSyncService") {
    private lateinit var mLanguagesSyncTasks: LanguagesSyncTasks
    private lateinit var mFollowupSyncTasks: FollowupSyncTasks

    // region Lifecycle Events

    override fun onCreate() {
        super.onCreate()
        mLanguagesSyncTasks = LanguagesSyncTasks.getInstance(this)
        mFollowupSyncTasks = FollowupSyncTasks.getInstance(this)
    }

    override fun onHandleSyncIntent(intent: Intent) {
        try {
            val args = intent.extras ?: Bundle.EMPTY
            when (intent.getIntExtra(EXTRA_SYNCTYPE, SYNCTYPE_NONE)) {
                SYNCTYPE_FOLLOWUPS -> try {
                    mFollowupSyncTasks.syncFollowups()
                } catch (e: IOException) {
                    scheduleSyncFollowupWork()
                    throw e
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
