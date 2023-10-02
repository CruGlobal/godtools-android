package org.cru.godtools.sync

import androidx.annotation.VisibleForTesting
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
import org.cru.godtools.sync.task.AnalyticsSyncTasks
import org.cru.godtools.sync.task.BaseSyncTasks
import org.cru.godtools.sync.task.FollowupSyncTasks
import org.cru.godtools.sync.task.LanguagesSyncTasks
import org.cru.godtools.sync.task.ToolSyncTasks
import org.cru.godtools.sync.task.UserCounterSyncTasks
import org.cru.godtools.sync.task.UserFavoriteToolsSyncTasks
import org.cru.godtools.sync.task.UserSyncTasks
import org.cru.godtools.sync.work.scheduleSyncDirtyFavoriteToolsWork
import org.cru.godtools.sync.work.scheduleSyncFollowupsWork
import org.cru.godtools.sync.work.scheduleSyncLanguagesWork
import org.cru.godtools.sync.work.scheduleSyncToolSharesWork
import org.cru.godtools.sync.work.scheduleSyncToolsWork
import timber.log.Timber

private const val TAG = "GodToolsSyncService"
private const val SYNC_PARALLELISM = 8

@Singleton
class GodToolsSyncService @VisibleForTesting internal constructor(
    workManager: Lazy<WorkManager>,
    private val syncTasks: Map<Class<out BaseSyncTasks>, Provider<BaseSyncTasks>>,
    private val coroutineDispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope = CoroutineScope(coroutineDispatcher + SupervisorJob()),
) {
    @Inject
    @OptIn(ExperimentalCoroutinesApi::class)
    internal constructor(
        workManager: Lazy<WorkManager>,
        syncTasks: Map<Class<out BaseSyncTasks>, @JvmSuppressWildcards Provider<BaseSyncTasks>>,
    ) : this(workManager, syncTasks, Dispatchers.IO.limitedParallelism(SYNC_PARALLELISM))

    private val workManager by workManager

    private inline fun <reified T : BaseSyncTasks, R : Any?> with(block: T.() -> R) =
        requireNotNull(syncTasks[T::class.java]?.get() as? T) { "${T::class.simpleName} not injected" }.block()

    private suspend inline fun <reified T : BaseSyncTasks> executeSync(
        crossinline block: suspend T.() -> Boolean,
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
    suspend fun syncLanguages(force: Boolean = false) = try {
        executeSync<LanguagesSyncTasks> { syncLanguages(force) }
            .also { if (!it) workManager.scheduleSyncLanguagesWork() }
    } catch (e: CancellationException) {
        workManager.scheduleSyncLanguagesWork()
        throw e
    }

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

    suspend fun syncFavoriteTools(force: Boolean) = try {
        executeSync<UserFavoriteToolsSyncTasks> { syncFavoriteTools(force) }
    } finally {
        coroutineScope.launch { syncDirtyFavoriteTools() }
    }
    suspend fun syncDirtyFavoriteTools() = try {
        executeSync<UserFavoriteToolsSyncTasks> { syncDirtyFavoriteTools() }
            .also { if (!it) workManager.scheduleSyncDirtyFavoriteToolsWork() }
    } catch (e: CancellationException) {
        workManager.scheduleSyncDirtyFavoriteToolsWork()
        throw e
    }

    fun syncToolSharesAsync() = coroutineScope.async { syncToolShares() }
    private suspend fun syncToolShares() = try {
        executeSync<ToolSyncTasks> { syncShares() }.also { if (!it) workManager.scheduleSyncToolSharesWork() }
    } catch (e: CancellationException) {
        workManager.scheduleSyncToolSharesWork()
        throw e
    }
    // endregion Sync Tasks
}
