package org.cru.godtools.sync

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.sync.SyncRegistry
import org.ccci.gto.android.common.sync.SyncTask
import org.ccci.gto.android.common.sync.event.SyncFinishedEvent
import org.cru.godtools.base.util.SingletonHolder
import org.cru.godtools.sync.task.AnalyticsSyncTasks
import org.cru.godtools.sync.task.FollowupSyncTasks
import org.cru.godtools.sync.task.LanguagesSyncTasks
import org.cru.godtools.sync.task.ToolSyncTasks
import org.cru.godtools.sync.work.scheduleSyncFollowupWork
import org.cru.godtools.sync.work.scheduleSyncToolSharesWork
import org.greenrobot.eventbus.EventBus
import java.io.IOException

private const val EXTRA_SYNCTYPE = "org.cru.godtools.sync.GodToolsSyncService.EXTRA_SYNCTYPE"
private const val SYNCTYPE_NONE = 0
private const val SYNCTYPE_LANGUAGES = 2
private const val SYNCTYPE_TOOLS = 3
private const val SYNCTYPE_FOLLOWUPS = 4
private const val SYNCTYPE_TOOL_SHARES = 5
private const val SYNCTYPE_GLOBAL_ACTIVITY = 6

class GodToolsSyncService private constructor(private val context: Context) : CoroutineScope {
    companion object : SingletonHolder<GodToolsSyncService, Context>({ GodToolsSyncService(it.applicationContext) })

    private val job = SupervisorJob()
    override val coroutineContext get() = Dispatchers.IO + job

    private val eventBus = EventBus.getDefault()

    private val analyticsSyncTasks by lazy { AnalyticsSyncTasks.getInstance(context) }
    private val followupSyncTasks by lazy { FollowupSyncTasks.getInstance(context) }
    private val languageSyncTasks by lazy { LanguagesSyncTasks.getInstance(context) }
    private val toolSyncTasks by lazy { ToolSyncTasks.getInstance(context) }

    private fun processSyncTask(task: GtSyncTask): Int {
        val syncId = SyncRegistry.startSync()
        GlobalScope.launch {
            try {
                when (task.args.getInt(EXTRA_SYNCTYPE, SYNCTYPE_NONE)) {
                    SYNCTYPE_LANGUAGES -> languageSyncTasks.syncLanguages(task.args)
                    SYNCTYPE_TOOLS -> toolSyncTasks.syncTools(task.args)
                    SYNCTYPE_TOOL_SHARES -> if (!toolSyncTasks.syncShares()) context.scheduleSyncToolSharesWork()
                    SYNCTYPE_FOLLOWUPS -> if (!followupSyncTasks.syncFollowups()) context.scheduleSyncFollowupWork()
                    SYNCTYPE_GLOBAL_ACTIVITY -> analyticsSyncTasks.syncGlobalActivity(task.args)
                }
            } catch (e: IOException) {
                // TODO: should we queue up work tasks here because of the IOException?
            } finally {
                SyncRegistry.finishSync(syncId)
                eventBus.post(SyncFinishedEvent(syncId))
            }
        }

        return syncId
    }

    // region Sync Tasks
    fun syncLanguages(force: Boolean): SyncTask = GtSyncTask(Bundle(2).apply {
        putInt(EXTRA_SYNCTYPE, SYNCTYPE_LANGUAGES)
        putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, force)
    })

    fun syncTools(force: Boolean): SyncTask = GtSyncTask(Bundle(2).apply {
        putInt(EXTRA_SYNCTYPE, SYNCTYPE_TOOLS)
        putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, force)
    })

    fun syncToolShares(): SyncTask = GtSyncTask(Bundle(1).apply {
        putInt(EXTRA_SYNCTYPE, SYNCTYPE_TOOL_SHARES)
    })

    fun syncFollowups(): SyncTask = GtSyncTask(Bundle(1).apply {
        putInt(EXTRA_SYNCTYPE, SYNCTYPE_FOLLOWUPS)
    })

    fun syncGlobalActivity(force: Boolean = false): SyncTask = GtSyncTask(Bundle(2).apply {
        putInt(EXTRA_SYNCTYPE, SYNCTYPE_GLOBAL_ACTIVITY)
        putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, force)
    })

    private inner class GtSyncTask(internal val args: Bundle) : SyncTask {
        override fun sync() = processSyncTask(this)
    }
    // endregion Sync Tasks
}
