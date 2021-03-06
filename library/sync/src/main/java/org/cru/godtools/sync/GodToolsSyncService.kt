package org.cru.godtools.sync

import android.content.ContentResolver
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.core.os.bundleOf
import androidx.work.WorkManager
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.sync.SyncRegistry
import org.ccci.gto.android.common.sync.SyncTask
import org.ccci.gto.android.common.sync.event.SyncFinishedEvent
import org.cru.godtools.sync.task.AnalyticsSyncTasks
import org.cru.godtools.sync.task.BaseSyncTasks
import org.cru.godtools.sync.task.FollowupSyncTasks
import org.cru.godtools.sync.task.LanguagesSyncTasks
import org.cru.godtools.sync.task.ToolSyncTasks
import org.cru.godtools.sync.work.scheduleSyncFollowupWork
import org.cru.godtools.sync.work.scheduleSyncToolSharesWork
import org.greenrobot.eventbus.EventBus

private const val EXTRA_SYNCTYPE = "org.cru.godtools.sync.GodToolsSyncService.EXTRA_SYNCTYPE"
private const val SYNCTYPE_NONE = 0
private const val SYNCTYPE_LANGUAGES = 2
private const val SYNCTYPE_TOOLS = 3
private const val SYNCTYPE_FOLLOWUPS = 4
private const val SYNCTYPE_TOOL_SHARES = 5
private const val SYNCTYPE_GLOBAL_ACTIVITY = 6

@Singleton
class GodToolsSyncService @VisibleForTesting internal constructor(
    private val eventBus: EventBus,
    private val workManager: WorkManager,
    private val syncTasks: Map<Class<out BaseSyncTasks>, @JvmSuppressWildcards Provider<BaseSyncTasks>>,
    private val coroutineScope: CoroutineScope
) {
    @Inject
    internal constructor(
        eventBus: EventBus,
        workManager: WorkManager,
        syncTasks: Map<Class<out BaseSyncTasks>, @JvmSuppressWildcards Provider<BaseSyncTasks>>
    ) : this(eventBus, workManager, syncTasks, CoroutineScope(Dispatchers.IO + SupervisorJob()))

    private fun processSyncTask(task: GtSyncTask): Int {
        val syncId = SyncRegistry.startSync()
        coroutineScope.launch {
            try {
                when (task.args.getInt(EXTRA_SYNCTYPE, SYNCTYPE_NONE)) {
                    SYNCTYPE_LANGUAGES -> with<LanguagesSyncTasks> { syncLanguages(task.args) }
                    SYNCTYPE_TOOLS -> with<ToolSyncTasks> { syncTools(task.args) }
                    SYNCTYPE_TOOL_SHARES -> with<ToolSyncTasks> {
                        if (!syncShares()) workManager.scheduleSyncToolSharesWork()
                    }
                    SYNCTYPE_FOLLOWUPS -> with<FollowupSyncTasks> {
                        if (!syncFollowups()) workManager.scheduleSyncFollowupWork()
                    }
                    SYNCTYPE_GLOBAL_ACTIVITY -> with<AnalyticsSyncTasks> { syncGlobalActivity(task.args) }
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

    private inline fun <reified T : BaseSyncTasks> with(block: T.() -> Unit) =
        requireNotNull(syncTasks[T::class.java]?.get() as? T) { "${T::class.simpleName} not injected" }.block()

    // region Sync Tasks
    fun syncLanguages(force: Boolean): SyncTask = GtSyncTask(
        bundleOf(
            EXTRA_SYNCTYPE to SYNCTYPE_LANGUAGES,
            ContentResolver.SYNC_EXTRAS_MANUAL to force
        )
    )

    fun syncTools(force: Boolean): SyncTask = GtSyncTask(
        bundleOf(
            EXTRA_SYNCTYPE to SYNCTYPE_TOOLS,
            ContentResolver.SYNC_EXTRAS_MANUAL to force
        )
    )

    fun syncGlobalActivity(force: Boolean = false): SyncTask = GtSyncTask(
        bundleOf(
            EXTRA_SYNCTYPE to SYNCTYPE_GLOBAL_ACTIVITY,
            ContentResolver.SYNC_EXTRAS_MANUAL to force
        )
    )

    fun syncToolShares(): SyncTask = GtSyncTask(bundleOf(EXTRA_SYNCTYPE to SYNCTYPE_TOOL_SHARES))
    fun syncFollowups(): SyncTask = GtSyncTask(bundleOf(EXTRA_SYNCTYPE to SYNCTYPE_FOLLOWUPS))

    private inner class GtSyncTask(val args: Bundle) : SyncTask {
        override fun sync() = processSyncTask(this)
    }
    // endregion Sync Tasks
}
