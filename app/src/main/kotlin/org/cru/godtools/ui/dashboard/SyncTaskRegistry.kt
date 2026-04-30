package org.cru.godtools.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.CircuitContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.ccci.gto.android.common.sync.SyncTracker
import org.ccci.gto.android.common.sync.rememberSyncTracker

internal class SyncTaskRegistry(val syncTracker: SyncTracker) {
    private val tasks = mutableMapOf<String, SyncTracker.(force: Boolean) -> Unit>()

    @OptIn(ExperimentalUuidApi::class)
    fun registerSyncTask(task: SyncTracker.(force: Boolean) -> Unit): String {
        val id = Uuid.generateV7().toString()
        synchronized(tasks) { tasks[id] = task }
        syncTracker.task(false)
        return id
    }

    fun unregisterSyncTask(id: String) {
        synchronized(tasks) { tasks.remove(id) }
    }

    fun triggerSyncTasks(force: Boolean = false) {
        synchronized(tasks) { tasks.values.toList() }.forEach { syncTracker.it(force) }
    }

    companion object {
        internal var CircuitContext.syncTaskRegistry: SyncTaskRegistry?
            get() = tag() ?: parent?.syncTaskRegistry
            set(value) = putTag(value)

        @Composable
        internal fun rememberSyncRegistry(): SyncTaskRegistry {
            val syncTracker = rememberSyncTracker()
            return remember(syncTracker) { SyncTaskRegistry(syncTracker) }
        }
    }
}
