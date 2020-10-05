package org.cru.godtools.download.manager

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.util.ThreadUtils
import org.cru.godtools.base.FileManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.model.event.AttachmentUpdateEvent
import org.cru.godtools.model.event.ToolUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.AttachmentTable
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

    // region Temporary migration logic
    @JvmField
    protected val LOCK_FILESYSTEM: ReadWriteLock = ReentrantReadWriteLock()
    @JvmField
    protected val LOCKS_FILES = ArrayMap<String, Any>()
    // endregion Temporary migration logic

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

    // region Download Progress
    private val downloadProgressLiveData = mutableMapOf<TranslationKey, MutableLiveData<DownloadProgress?>>()

    @AnyThread
    private fun getDownloadProgressLiveData(translation: TranslationKey) = synchronized(downloadProgressLiveData) {
        downloadProgressLiveData.getOrPut(translation) { DownloadProgressLiveData() }
    }

    @MainThread
    fun getDownloadProgressLiveData(tool: String, locale: Locale): LiveData<DownloadProgress?> =
        getDownloadProgressLiveData(TranslationKey(tool, locale))

    @AnyThread
    @VisibleForTesting
    fun startProgress(translation: TranslationKey) {
        getDownloadProgressLiveData(translation).postValue(DownloadProgress.INITIAL)
    }

    @AnyThread
    @VisibleForTesting
    fun updateProgress(translation: TranslationKey, progress: Long, max: Long) {
        getDownloadProgressLiveData(translation).postValue(DownloadProgress(progress, max))
    }

    @AnyThread
    @VisibleForTesting
    fun finishDownload(translation: TranslationKey) {
        getDownloadProgressLiveData(translation).postValue(null)
    }
    // endregion Download Progress

    // region Attachments
    @WorkerThread
    @Throws(IOException::class)
    fun importAttachment(attachment: Attachment, data: InputStream) {
        if (!fileManager.createResourcesDir()) return

        val filename = attachment.localFilename ?: return
        val lock = LOCK_FILESYSTEM.readLock()
        try {
            lock.lock()
            synchronized(ThreadUtils.getLock(LOCKS_FILES, filename)) {
                runBlocking {
                    // short-circuit if the attachment is already downloaded
                    val localFile: LocalFile? = dao.find(filename)
                    if (attachment.isDownloaded && localFile != null) return@runBlocking

                    // download the attachment
                    attachment.isDownloaded = false
                    try {
                        // we don't have a local file, so create it
                        if (localFile == null) {
                            val file = LocalFile(filename)
                            data.copyTo(file)
                            dao.updateOrInsert(file)
                        }

                        // mark attachment as downloaded
                        attachment.isDownloaded = true
                    } finally {
                        // update attachment download state
                        dao.update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
                        eventBus.post(AttachmentUpdateEvent)
                    }
                }
            }
        } finally {
            lock.unlock()
        }
    }

    @WorkerThread
    @VisibleForTesting
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
    // endregion Attachments
}
