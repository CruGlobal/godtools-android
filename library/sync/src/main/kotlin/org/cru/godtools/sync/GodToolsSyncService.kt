package org.cru.godtools.sync

import android.content.ContentResolver
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import androidx.core.os.bundleOf
import androidx.work.WorkManager
import dagger.Lazy
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.dagger.getValue
import org.ccci.gto.android.common.sync.SyncRegistry
import org.ccci.gto.android.common.sync.SyncTask
import org.ccci.gto.android.common.sync.event.SyncFinishedEvent
import org.cru.godtools.sync.task.AnalyticsSyncTasks
import org.cru.godtools.sync.task.BaseSyncTasks
import org.cru.godtools.sync.task.FollowupSyncTasks
import org.cru.godtools.sync.task.LanguagesSyncTasks
import org.cru.godtools.sync.task.ToolSyncTasks
import org.cru.godtools.sync.task.UserCounterSyncTasks
import org.cru.godtools.sync.task.UserSyncTasks
import org.cru.godtools.sync.work.scheduleSyncFollowupsWork
import org.cru.godtools.sync.work.scheduleSyncLanguagesWork
import org.cru.godtools.sync.work.scheduleSyncToolSharesWork
import org.cru.godtools.sync.work.scheduleSyncToolsWork
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

private const val TAG = "GodToolsSyncService"
private const val SYNC_PARALLELISM = 8

private const val EXTRA_SYNCTYPE = "org.cru.godtools.sync.GodToolsSyncService.EXTRA_SYNCTYPE"
private const val SYNCTYPE_NONE = 0
private const val SYNCTYPE_LANGUAGES = 2
private const val SYNCTYPE_TOOL_SHARES = 5

@Singleton
class GodToolsSyncService @VisibleForTesting internal constructor(
    private val eventBus: EventBus,
    workManager: Lazy<WorkManager>,
    private val syncTasks: Map<Class<out BaseSyncTasks>, @JvmSuppressWildcards Provider<BaseSyncTasks>>,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope = CoroutineScope(coroutineDispatcher + SupervisorJob())
) {
    @Inject
    @OptIn(ExperimentalCoroutinesApi::class)
    internal constructor(
        eventBus: EventBus,
        workManager: Lazy<WorkManager>,
        syncTasks: Map<Class<out BaseSyncTasks>, @JvmSuppressWildcards Provider<BaseSyncTasks>>
    ) : this(eventBus, workManager, syncTasks, Dispatchers.IO.limitedParallelism(SYNC_PARALLELISM))

    private val workManager by workManager

    private suspend fun executeSyncTask(task: GtSyncTask, syncId: Int? = null): Unit =
        withContext(coroutineDispatcher) {
            val syncType = task.args.getInt(EXTRA_SYNCTYPE, SYNCTYPE_NONE)
            try {
                when (syncType) {
                    SYNCTYPE_LANGUAGES -> with<LanguagesSyncTasks> {
                        if (!syncLanguages(task.args)) workManager.scheduleSyncLanguagesWork()
                    }
                    SYNCTYPE_TOOL_SHARES -> with<ToolSyncTasks> {
                        if (!syncShares()) workManager.scheduleSyncToolSharesWork()
                    }
                }
            } catch (e: IOException) {
                // queue up work tasks here because of the IOException
                when (syncType) {
                    SYNCTYPE_LANGUAGES -> workManager.scheduleSyncLanguagesWork()
                    SYNCTYPE_TOOL_SHARES -> workManager.scheduleSyncToolSharesWork()
                }
            } finally {
                if (syncId != null) {
                    SyncRegistry.finishSync(syncId)
                    eventBus.post(SyncFinishedEvent(syncId))
                }
            }
        }

    private inline fun <reified T : BaseSyncTasks> with(block: T.() -> Unit) = with<T, Unit>(block)
    private inline fun <reified T : BaseSyncTasks, R : Any?> with(block: T.() -> R) =
        requireNotNull(syncTasks[T::class.java]?.get() as? T) { "${T::class.simpleName} not injected" }.block()

    private suspend inline fun <reified T : BaseSyncTasks> executeSync(
        crossinline block: suspend T.() -> Boolean
    ) = withContext(coroutineDispatcher) {
        try {
            with<T, Boolean> { block() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            false
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Unhandled sync exception")
            false
        }
    }

    // region Sync Tasks
    fun syncLanguages(force: Boolean): SyncTask = GtSyncTask(
        bundleOf(
            EXTRA_SYNCTYPE to SYNCTYPE_LANGUAGES,
            ContentResolver.SYNC_EXTRAS_MANUAL to force
        )
    )

    suspend fun syncTools(force: Boolean) = try {
        executeSync<ToolSyncTasks> { syncTools(force) }.also { if (!it) workManager.scheduleSyncToolsWork() }
    } catch (e: CancellationException) {
        workManager.scheduleSyncToolsWork()
        throw e
    }

    suspend fun syncTool(toolCode: String, force: Boolean = false) =
        executeSync<ToolSyncTasks> { syncTool(toolCode, force) }

    suspend fun syncGlobalActivity(force: Boolean = false) =
        executeSync<AnalyticsSyncTasks> { syncGlobalActivity(force) }

    suspend fun syncUser(force: Boolean = false) = executeSync<UserSyncTasks> { syncUser(force) }

    fun syncFollowupsAsync() = coroutineScope.async { syncFollowups() }
    private suspend fun syncFollowups() = executeSync<FollowupSyncTasks> { syncFollowups() }
        .also { if (!it) workManager.scheduleSyncFollowupsWork() }

    suspend fun syncDirtyUserCounters() = executeSync<UserCounterSyncTasks> { syncDirtyCounters() }
    suspend fun syncUserCounters(force: Boolean = false) = executeSync<UserCounterSyncTasks> {
        try {
            syncCounters(force)
        } finally {
            coroutineScope.launch { syncDirtyUserCounters() }
        }
    }

    fun syncToolShares(): SyncTask = GtSyncTask(bundleOf(EXTRA_SYNCTYPE to SYNCTYPE_TOOL_SHARES))

    private inner class GtSyncTask(val args: Bundle) : SyncTask {
        override fun sync(): Int {
            val syncId = SyncRegistry.startSync()
            coroutineScope.launch { executeSyncTask(this@GtSyncTask, syncId) }
            return syncId
        }
    }
    // endregion Sync Tasks
}
