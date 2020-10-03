package org.cru.godtools.download.manager

import androidx.annotation.AnyThread
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.annotation.WorkerThread
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.cru.godtools.base.FileManager
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.event.ToolUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

private const val TAG = "GodToolsDownloadManager"

open class KotlinGodToolsDownloadManager(
    private val dao: GodToolsDao,
    private val eventBus: EventBus,
    private val fileManager: FileManager
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

    @WorkerThread
    @VisibleForTesting(otherwise = PRIVATE)
    @Throws(IOException::class)
    fun InputStream.copyTo(localFile: LocalFile) {
        buffered().use { buffer ->
            val file = localFile.getFile(fileManager)
                ?: throw FileNotFoundException("${localFile.filename} (File could not be created)")

            file.outputStream().use { out ->
                buffer.copyTo(out)
                dao.updateOrInsert(localFile)
            }
        }
    }
}
