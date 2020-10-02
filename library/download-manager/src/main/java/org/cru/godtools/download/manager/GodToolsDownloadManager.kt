package org.cru.godtools.download.manager

import androidx.annotation.AnyThread
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cru.godtools.model.Tool
import org.cru.godtools.model.event.ToolUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

private const val TAG = "GodToolsDownloadManager"

open class KotlinGodToolsDownloadManager(
    protected val dao: GodToolsDao,
    protected val eventBus: EventBus
) : CoroutineScope {
    @VisibleForTesting
    internal val job = SupervisorJob()
    override val coroutineContext get() = Dispatchers.Default + job

    // region Tool/Language pinning
    @AnyThread
    fun pinToolAsync(code: String) {
        launch { pinTool(code) }
    }

    suspend fun pinTool(code: String) {
        val tool = Tool().also {
            it.code = code
            it.isAdded = true
        }
        withContext(Dispatchers.IO) { dao.update(tool, ToolTable.COLUMN_ADDED) }
        eventBus.post(ToolUpdateEvent)
    }
    // endregion Tool/Language pinning
}
