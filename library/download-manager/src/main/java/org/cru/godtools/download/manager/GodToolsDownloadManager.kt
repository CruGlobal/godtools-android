package org.cru.godtools.download.manager

import androidx.annotation.AnyThread
import androidx.annotation.GuardedBy
import androidx.annotation.MainThread
import androidx.annotation.VisibleForTesting
import androidx.annotation.WorkerThread
import androidx.collection.ArrayMap
import androidx.collection.LongSparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.io.CountingInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.zip.ZipInputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.ccci.gto.android.common.util.ThreadUtils
import org.cru.godtools.api.AttachmentsApi
import org.cru.godtools.base.FileManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Language
import org.cru.godtools.model.LocalFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationFile
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.model.event.AttachmentUpdateEvent
import org.cru.godtools.model.event.LanguageUpdateEvent
import org.cru.godtools.model.event.ToolUpdateEvent
import org.cru.godtools.model.event.TranslationUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

private const val TAG = "GodToolsDownloadManager"

open class KotlinGodToolsDownloadManager(
    private val attachmentsApi: AttachmentsApi,
    private val dao: GodToolsDao,
    private val eventBus: EventBus,
    private val fileManager: FileManager
) : CoroutineScope {
    @VisibleForTesting
    internal val job = SupervisorJob()
    override val coroutineContext get() = Dispatchers.Default + job

    private val filesMutex = MutexMap()

    // region Temporary migration logic
    @JvmField
    protected val LOCKS_ATTACHMENTS = LongSparseArray<Any>()
    @JvmField
    protected val LOCK_FILESYSTEM: ReadWriteLock = ReentrantReadWriteLock()
    @JvmField
    protected val LOCKS_TRANSLATION_DOWNLOADS = ArrayMap<TranslationKey, Any>()
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

    @AnyThread
    fun pinLanguageAsync(locale: Locale) {
        launch { pinLanguage(locale) }
    }

    suspend fun pinLanguage(locale: Locale) {
        val language = Language().apply {
            code = locale
            isAdded = true
        }
        withContext(Dispatchers.IO) { dao.update(language, LanguageTable.COLUMN_ADDED) }
        eventBus.post(LanguageUpdateEvent)
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
    @VisibleForTesting
    fun downloadAttachment(attachmentId: Long) {
        if (runBlocking { !fileManager.createResourcesDir() }) return

        synchronized(ThreadUtils.getLock(LOCKS_ATTACHMENTS, attachmentId)) {
            val attachment: Attachment = dao.find(attachmentId) ?: return
            val filename = attachment.localFilename ?: return

            val lock = LOCK_FILESYSTEM.readLock()
            try {
                lock.lock()
                runBlocking {
                    filesMutex.withLock(filename) {
                        val localFile = dao.find<LocalFile>(filename)
                        if (attachment.isDownloaded && localFile != null) return@runBlocking
                        attachment.isDownloaded = localFile != null

                        if (localFile == null) {
                            // create a new local file object
                            try {
                                // download attachment
                                attachmentsApi.download(attachmentId).takeIf { it.isSuccessful }?.body()?.byteStream()
                                    ?.use {
                                        val file = LocalFile(filename)
                                        it.copyTo(file)
                                        dao.updateOrInsert(file)
                                        attachment.isDownloaded = true
                                    }
                            } catch (ignored: IOException) {
                            }
                        }

                        dao.update(attachment, AttachmentTable.COLUMN_DOWNLOADED)
                        eventBus.post(AttachmentUpdateEvent)
                    }
                }
            } finally {
                lock.unlock()
            }
        }
    }

    @WorkerThread
    fun importAttachment(attachment: Attachment, data: InputStream) {
        if (runBlocking { !fileManager.createResourcesDir() }) return

        val filename = attachment.localFilename ?: return
        val lock = LOCK_FILESYSTEM.readLock()
        try {
            lock.lock()
            runBlocking {
                filesMutex.withLock(filename) {
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
    // endregion Attachments

    // region Translations
    @WorkerThread
    @Throws(IOException::class)
    fun storeTranslation(translation: Translation, zipStream: InputStream, size: Long) {
        if (runBlocking { !fileManager.createResourcesDir() }) return

        // lock translation
        val key = TranslationKey(translation)
        synchronized(ThreadUtils.getLock(LOCKS_TRANSLATION_DOWNLOADS, key)) {
            // track the start of this download
            startProgress(key)
            val lock = LOCK_FILESYSTEM.readLock()
            try {
                lock.lock()

                // process the download
                runBlocking { zipStream.extractZipFor(translation, size) }

                // mark translation as downloaded
                translation.isDownloaded = true
                dao.update(translation, TranslationTable.COLUMN_DOWNLOADED)
                eventBus.post(TranslationUpdateEvent)
            } finally {
                lock.unlock()

                // finish the download
                finishDownload(key)
            }
        }
    }

    @AnyThread
    @GuardedBy("LOCK_FILESYSTEM")
    @VisibleForTesting
    internal suspend fun InputStream.extractZipFor(translation: Translation, zipSize: Long = -1L) {
        if (!fileManager.createResourcesDir()) return

        val translationKey = TranslationKey(translation)
        withContext(Dispatchers.IO) {
            val count = CountingInputStream(this@extractZipFor)
            ZipInputStream(count.buffered()).use { zin ->
                while (true) {
                    val ze = zin.nextEntry ?: break
                    val filename = ze.name
                    filesMutex.withLock(filename) {
                        // write the file if it hasn't been downloaded before
                        if (dao.find<LocalFile>(filename) == null) {
                            val newFile = LocalFile(filename)
                            zin.copyTo(newFile)
                            dao.updateOrInsert(newFile)
                        }

                        // associate this file with this translation
                        dao.updateOrInsert(TranslationFile(translation, filename))
                    }
                    updateProgress(translationKey, count.count, zipSize)
                }
            }
        }
    }
    // endregion Translations

    @WorkerThread
    private suspend fun InputStream.copyTo(localFile: LocalFile) {
        val file = localFile.getFile(fileManager)
            ?: throw FileNotFoundException("${localFile.filename} (File could not be created)")

        withContext(Dispatchers.IO) { file.outputStream().use { copyTo(it) } }
    }
}
